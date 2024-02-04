package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Promise
import io.github.andjsrk.v4.evaluate.type.*

class PromiseType: ObjectType(lazy { Promise.instancePrototype }) {
    var state: State? = null
    var result: LanguageType? = null
    val fulfillReactions = mutableListOf<Reaction>()
    val rejectReactions = mutableListOf<Reaction>()
    var isHandled = false

    @EsSpec("FulfillPromise")
    fun fulfill(value: LanguageType) {
        state = State.FULFILLED
        result = value
        for (reaction in fulfillReactions) jobQueue += Reaction.Job.new(reaction, value)
    }
    @EsSpec("RejectPromise")
    fun reject(reason: LanguageType) {
        state = State.REJECTED
        result = reason
        for (reaction in rejectReactions) jobQueue += Reaction.Job.new(reaction, reason)
    }
    @EsSpec("PerformPromiseThen")
    fun then(onFulfilled: FunctionType?, onRejected: FunctionType?, resultCapability: Capability? = null): LanguageType {
        val fulfillReaction = Reaction(resultCapability, State.FULFILLED, onFulfilled)
        val rejectReaction = Reaction(resultCapability, State.REJECTED, onRejected)
        when (state) {
            null -> {
                if (onFulfilled != null) fulfillReactions += fulfillReaction
                if (onRejected != null) rejectReactions += rejectReaction
            }
            State.FULFILLED -> {
                if (onFulfilled != null) {
                    val fulfillJob = Reaction.Job.new(fulfillReaction, result!!)
                    jobQueue += fulfillJob
                }
            }
            State.REJECTED -> {
                if (onRejected != null) {
                    val rejectJob = Reaction.Job.new(rejectReaction, result!!)
                    jobQueue += rejectJob
                }
            }
        }
        return resultCapability?.promise ?: NullType
    }
    /**
     * Returns a pair of `resolve` and `reject`.
     */
    fun createResolveRejectFunction(): Pair<BuiltinFunctionType, BuiltinFunctionType> {
        val promise = this
        var alreadyResolved = false
        val resolve = functionWithoutThis("resolve") fn@ { args ->
            val value = args.getOptional(0) ?: NullType
            if (alreadyResolved) return@fn normalNull
            alreadyResolved = true
            if (sameValue(value, promise)) {
                reject(error(TypeErrorKind.PROMISE_CYCLIC, promise.display()))
                return@fn normalNull
            }
            if (value is PromiseType) jobQueue += createThenableJob(value)
            else fulfill(value)
            normalNull
        }
        val reject = functionWithoutThis("reject") fn@ { args ->
            val reason = args.getOptional(0) ?: NullType
            if (alreadyResolved) return@fn normalNull
            alreadyResolved = true
            reject(reason)
            normalNull
        }
        return resolve to reject
    }
    @EsSpec("NewPromiseResolveThenableJob")
    fun createThenableJob(thenable: PromiseType) =
        Reaction.Job({
            val (resolve, reject) = createResolveRejectFunction()
            thenable.then(resolve, reject, Capability.new())
                .toNormal()
        }, runningExecutionContext.realm, getActiveModule())

    companion object {
        @EsSpec("PromiseResolve")
        fun resolve(value: LanguageType) =
            Capability.new().apply {
                resolve.callWithSingleArg(value)
                    .unwrap()
            }
                .promise
    }

    /**
     * Note: use `null` to represent `PENDING`.
     */
    enum class State {
        FULFILLED,
        REJECTED,
    }
    @EsSpec("PromiseCapability Record")
    data class Capability(
        val promise: PromiseType,
        val resolve: FunctionType,
        val reject: FunctionType,
    ): Record {
        @EsSpec("AsyncFunctionStart")
        fun startAsyncFunction(bodyEval: SimpleLazyFlow<Completion.FromFunctionBody<*>>) {
            val asyncContext = runningExecutionContext.copy()
            startAsyncBlock(bodyEval, asyncContext)
        }
        @EsSpec("AsyncBlockStart")
        fun startAsyncBlock(bodyEval: SimpleLazyFlow<Completion.FromFunctionBody<*>>, asyncContext: ExecutionContext) {
            val evalState = lazyFlow {
                val res = yieldAll(bodyEval)
                executionContextStack.removeTop()
                when (res) {
                    is Completion.WideNormal<*> -> resolve.callWithSingleArg(NullType)
                    is Completion.Return -> resolve.callWithSingleArg(res.value)
                    is Completion.Throw -> reject.callWithSingleArg(res.value)
                }
                    .unwrap()
                normalNull
            }
            asyncContext.codeEvaluationState = evalState
            executionContextStack.addTop(asyncContext)
            evalState.next()
        }

        companion object {
            @EsSpec("NewPromiseCapability")
            fun new(): Capability {
                val promise = PromiseType()
                val (resolve, reject) = promise.createResolveRejectFunction()
                return Capability(promise, resolve, reject)
            }
        }
    }
    @EsSpec("PromiseReaction Record")
    class Reaction(val capability: Capability?, val type: State, val handler: FunctionType?) {
        class Job(val closure: () -> MaybeEmpty, val realm: Realm?, val module: Module?) {
            companion object {
                @EsSpec("NewPromiseReactionJob")
                fun new(reaction: Reaction, value: LanguageType) =
                    Job({
                        val capability = reaction.capability
                        val handler = reaction.handler
                        val handlerRes =
                            handler?.call(NullType, listOf(value))
                                ?:
                                    if (reaction.type == State.FULFILLED) value.toNormal()
                                    else Completion.Throw(value)
                        if (capability == null) return@Job empty
                        val fn =
                            if (handlerRes is Completion.Abrupt) capability.reject
                            else capability.resolve
                        fn.callWithSingleArg(handlerRes.value as LanguageType)
                            .unwrap()
                            .toNormal()
                    }, reaction.handler?.realm, getActiveModule())
            }
        }
    }
}
