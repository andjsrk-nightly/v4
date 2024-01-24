package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Generator
import io.github.andjsrk.v4.evaluate.type.*

class SyncGeneratorType(
    override val brand: String? = null,
): GeneratorType<SyncGeneratorType.State>(lazy { Generator.instancePrototype }) {
    override val context = runningExecutionContext
    override var state: State? = null // [[GeneratorState]]
    @EsSpec("GeneratorStart")
    override fun start(result: SimpleLazyFlow<MaybeEmptyOrAbrupt>) {
        context.generator = this
        context.codeEvaluationState = lazyFlow f@ {
            val acGenContext = runningExecutionContext
            val acGenerator = acGenContext.generator as SyncGeneratorType? ?: neverHappens()
            val res = yieldAll(result)
            executionContextStack.removeTop()
            acGenerator.state = State.COMPLETED
            acGenContext.codeEvaluationState = null // drops the flow since it is no longer needed
            val resValue = when (res) {
                is Completion.WideNormal -> NullType
                is Completion.Return -> res.value
                is Completion.Abrupt -> return@f res
            }
            createIteratorResult(resValue, true)
                .toNormal()
        }
        state = State.SUSPENDED_START
    }
    /**
     * Note that `executionContextStack.removeTop()` is not needed because it will be performed by yield expression.
     *
     * @see syncYield
     */
    @EsSpec("GeneratorResume")
    fun resume(value: LanguageType?, brand: String? = null): NonEmptyOrAbrupt {
        validate(brand)
            .orReturn { return it }
        if (state == State.COMPLETED) return createIteratorResult(NullType, true).toNormal()
        assert(state.isOneOf(State.SUSPENDED_START, State.SUSPENDED_YIELD))
        state = State.EXECUTING
        executionContextStack.addTop(context)
        val res = context.codeEvaluationState!!.next(value.normalizeToNormal())
            .orReturn { return it }
        return res!!.toNormal()
    }
    @EsSpec("GeneratorValidate")
    override fun validate(brand: String?): EmptyOrAbrupt {
        if (this.brand != brand) return throwError(TypeErrorKind.INCOMPATIBLE_METHOD_RECEIVER, TODO(), TODO())
        if (state == State.EXECUTING) return throwError(TypeErrorKind.GENERATOR_RUNNING)
        return empty
    }

    enum class State {
        SUSPENDED_START,
        SUSPENDED_YIELD,
        EXECUTING,
        COMPLETED,
    }
}
