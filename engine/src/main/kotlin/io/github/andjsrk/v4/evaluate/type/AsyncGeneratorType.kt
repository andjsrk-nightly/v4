package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.AsyncGenerator
import io.github.andjsrk.v4.neverHappens

class AsyncGeneratorType(
    override var brand: String? = null,
): GeneratorType<AsyncGeneratorType.State>(lazy { AsyncGenerator.instancePrototype }) {
    override var context = runningExecutionContext
    override var state: State? = null // [[AsyncGeneratorState]]
    val queue = ArrayDeque<Request>()
    @EsSpec("AsyncGeneratorStart")
    override fun start(result: SimpleLazyFlow<Completion.FromFunctionBody<*>>) {
        val genContext = runningExecutionContext
        genContext.generator = this
        genContext.codeEvaluationState = lazyFlow f@ {
            val acGenContext = runningExecutionContext
            val acGenerator = acGenContext.generator!! as AsyncGeneratorType
            val res = yieldAll(result)
            executionContextStack.removeTop()
            acGenerator.state = State.COMPLETED
            acGenContext.codeEvaluationState = null
            val returnValue = when (res) {
                is Completion.WideNormal<*> -> normalNull
                is Completion.Return -> res.value.toNormal()
                else -> neverHappens()
            }
            completeStep(returnValue, true)
            normalNull
        }
        state = State.SUSPENDED_START
    }
    @EsSpec("AsyncGeneratorResume")
    fun resume(completion: Completion.FromFunctionBody<LanguageType>) {
        state = State.EXECUTING
        executionContextStack.addTop(context)
        context.codeEvaluationState?.next(completion)
    }
    @EsSpec("AsyncGeneratorCompleteStep")
    fun completeStep(completion: NonEmptyOrAbrupt, done: Boolean, realm: Realm? = null) {
        assert(queue.isNotEmpty())
        val next = queue.removeFirst()
        if (completion is Completion.Throw) {
            next.capability.reject.callWithSingleArg(completion.value)
                .unwrap()
        } else {
            require(completion is Completion.Normal)
            val iterRes =
                if (realm != null) {
                    val oldRealm = runningExecutionContext.realm
                    runningExecutionContext.realm = realm
                    val res = createIteratorResult(completion.value, done)
                    runningExecutionContext.realm = oldRealm
                    res
                } else createIteratorResult(completion.value, done)
            next.capability.resolve.callWithSingleArg(iterRes)
                .unwrap()
        }
    }
    @EsSpec("AsyncGeneratorAwaitReturn")
    fun awaitReturn() {
        assert(queue.isNotEmpty())
        val next = queue.first()
        require(next.completion is Completion.Return)
        val promise =
            PromiseType.resolve(next.completion.value)
        val onFulfilled = functionWithoutThis("", 1u) { args ->
            val value = args[0]
            state = State.COMPLETED
            completeStep(value.toNormal(), true)
            drainQueue()
            normalNull
        }
        val onRejected = functionWithoutThis("", 1u) { args ->
            val reason = args[0]
            state = State.COMPLETED
            completeStep(Completion.Throw(reason), true)
            drainQueue()
            normalNull
        }
        promise.then(onFulfilled, onRejected)
    }
    @EsSpec("AsyncGeneratorEnqueue")
    fun enqueue(completion: Completion.FromFunctionBody<LanguageType>, capability: PromiseType.Capability) {
        queue += Request(
            completion,
            capability
        )
    }
    @EsSpec("AsyncGeneratorDrainQueue")
    fun drainQueue() {
        assert(state == State.COMPLETED)
        if (queue.isEmpty()) return

        var done = false
        while (!done) {
            val next = queue.first()
            if (next.completion is Completion.Return) {
                state = State.AWAITING_RETURN
                next.completion
                awaitReturn()
                done = true
            } else {
                val comp =
                    when (next.completion) {
                        is Completion.WideNormal<*> -> normalNull
                        is Completion.Return -> next.completion
                        is Completion.Throw -> next.completion
                    }
                    if (next.completion is Completion.WideNormal<*>) normalNull
                    else next.completion
                completeStep(comp, true)
                if (queue.isEmpty()) done = true
            }
        }
    }
    @EsSpec("AsyncGeneratorValidate")
    override fun validate(brand: String?): EmptyOrThrow {
        if (this.brand != brand) return throwError(TypeErrorKind.INCOMPATIBLE_METHOD_RECEIVER, TODO(), TODO())
        return empty
    }

    @EsSpec("AsyncGeneratorRequest")
    data class Request(val completion: Completion.FromFunctionBody<LanguageType>, val capability: PromiseType.Capability)
    enum class State {
        SUSPENDED_START,
        SUSPENDED_YIELD,
        AWAITING_RETURN,
        EXECUTING,
        COMPLETED,
    }
}
