package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Promise
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens
import io.github.andjsrk.v4.parse.node.StatementListNode

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
                val fulfillJob = Reaction.Job.new(fulfillReaction, result!!)
                if (onFulfilled != null) jobQueue += fulfillJob
            }
            State.REJECTED -> {
                val rejectJob = Reaction.Job.new(rejectReaction, result!!)
                if (onRejected != null) jobQueue += rejectJob
            }
        }
        return resultCapability?.promise ?: NullType
    }
    /**
     * Returns a pair of `resolve` and `reject`.
     */
    fun createResolveFunction(): Pair<BuiltinFunctionType, BuiltinFunctionType> {
        val promise = this
        var alreadyResolved = false
        val resolve = BuiltinFunctionType("resolve") fn@ { _, args ->
            val value = args.getOptional(0) ?: NullType
            if (alreadyResolved) return@fn normalNull
            alreadyResolved = true
            if (sameValue(value, promise)) {
                reject(error(TypeErrorKind.PROMISE_CYCLIC, promise.display()))
                return@fn normalNull
            }
            if (value is PromiseType) TODO("Create job callback and enqueue it")
            else fulfill(value)
            normalNull
        }
        val reject = BuiltinFunctionType("reject") fn@ { _, args ->
            val reason = args.getOptional(0) ?: NullType
            if (alreadyResolved) return@fn normalNull
            alreadyResolved = true
            reject(reason)
            normalNull
        }
        return resolve to reject
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
        @EsSpec("AsyncBlockStart")
        fun asyncBlockStart(body: StatementListNode, asyncContext: ExecutionContext) {
            val closure = lazyFlow {
                val acAsyncContext = runningExecutionContext
                val result = yieldAll(evaluateStatements(body))
                executionContextStack.removeTop()
                when (result) {
                    is Completion.Normal -> resolve.call(null, listOf(NullType))
                    is Completion.Return -> resolve.call(null, listOf(result.value))
                    is Completion.Throw -> reject.call(null, listOf(result.value))
                    else -> neverHappens()
                }
                    .unwrap()
                    .toNormal()
            }
            // TODO: implement step 4
            executionContextStack.addTop(asyncContext)
        }

        companion object {
            @EsSpec("NewPromiseCapability")
            fun new(): Capability {
                val promise = PromiseType()
                val (resolve, reject) = promise.createResolveFunction()
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
                        fn.call(null, listOf(handlerRes.value as LanguageType))
                            .unwrap()
                            .toNormal()
                    }, reaction.handler?.realm, getActiveModule())
            }
        }
    }
}
