package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.AsyncGenerator
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens

class AsyncGeneratorType(
    override val brand: String? = null,
): GeneratorType<AsyncGeneratorState>(lazy { AsyncGenerator.instancePrototype }) {
    override val context = runningExecutionContext
    override var state: AsyncGeneratorState? = null // [[AsyncGeneratorState]]
    val queue = ArrayDeque<Request>()
    @EsSpec("AsyncGeneratorStart")
    override fun start(result: SimpleLazyFlow<MaybeEmptyOrAbrupt>) {
        val genContext = runningExecutionContext
        genContext.generator = this
        genContext.codeEvaluationState = lazyFlow f@ {
            val acGenContext = runningExecutionContext
            val acGenerator = acGenContext.generator!! as AsyncGeneratorType
            val res = yieldAll(result)
            executionContextStack.removeTop()
            acGenerator.state = AsyncGeneratorState.COMPLETED
            val returnValue = when (res) {
                is Completion.Normal -> normalNull
                is Completion.Return -> res.value.toNormal()
                else -> neverHappens()
            }
            completeStep(returnValue, true)
            normalNull
        }
        state = AsyncGeneratorState.SUSPENDED_START
    }
    @EsSpec("AsyncGeneratorResume")
    fun resume(completion: NonEmptyOrAbrupt) {
        state = AsyncGeneratorState.EXECUTING
        executionContextStack.addTop(context)
        context.codeEvaluationState?.next(completion)
    }
    @EsSpec("AsyncGeneratorCompleteStep")
    fun completeStep(completion: NonEmptyOrAbrupt, done: Boolean, realm: Realm? = null) {
        assert(queue.isNotEmpty())
        val next = queue.removeFirst()
        if (completion is Completion.Throw) {
            next.capability.reject.call(null, listOf(completion.value))
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
            next.capability.resolve.call(null, listOf(iterRes))
                .unwrap()
        }
    }
    @EsSpec("AsyncGeneratorAwaitReturn")
    fun awaitReturn() {
        assert(queue.isNotEmpty())
        val next = queue.first()
        require(next.completion is Completion.Return)
        val promise = PromiseType.resolve(next.completion.value)
        val onFulfilled = functionWithoutThis("", 1u) { args ->
            val value = args[0]
            state = AsyncGeneratorState.COMPLETED
            completeStep(value.toNormal(), true)
            drainQueue()
            normalNull
        }
        val onRejected = functionWithoutThis("", 1u) { args ->
            val reason = args[0]
            state = AsyncGeneratorState.COMPLETED
            completeStep(Completion.Throw(reason), true)
            drainQueue()
            normalNull
        }
        promise.then(onFulfilled, onRejected)
    }
    fun enqueue(completion: NonEmptyOrAbrupt, capability: PromiseType.Capability) {
        queue += Request(completion, capability)
    }
    @EsSpec("AsyncGeneratorDrainQueue")
    fun drainQueue() {
        assert(state == AsyncGeneratorState.COMPLETED)
        if (queue.isEmpty()) return

        var done = false
        while (!done) {
            val next = queue.first()
            if (next.completion is Completion.Return) {
                state = AsyncGeneratorState.AWAITING_RETURN
                next.completion
                awaitReturn()
                done = true
            } else {
                val comp =
                    if (next.completion is Completion.Normal) normalNull
                    else next.completion
                completeStep(comp, true)
                if (queue.isEmpty()) done = true
            }
        }
    }
    @EsSpec("AsyncGeneratorValidate")
    fun validate(brand: String? = null): EmptyOrAbrupt {
        if (this.brand != brand) return throwError(TypeErrorKind.INCOMPATIBLE_METHOD_RECEIVER, TODO(), TODO())
        return empty
    }

    data class Request(val completion: NonEmptyOrAbrupt, val capability: PromiseType.Capability)
}
