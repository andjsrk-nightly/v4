package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Generator
import io.github.andjsrk.v4.evaluate.type.*

class SyncGeneratorType(
    override val brand: String? = null,
): GeneratorType<SyncGeneratorState>(lazy { Generator.instancePrototype }) {
    override var context = runningExecutionContext
    override var state: SyncGeneratorState? = null // [[GeneratorState]]
    var complete: SimpleLazyFlow<NonEmptyOrAbrupt>? = null
    @EsSpec("GeneratorStart")
    override fun start(result: SimpleLazyFlow<MaybeEmptyOrAbrupt>) {
        val genContext = runningExecutionContext
        genContext.generator = this
        complete = lazyFlow f@ {
            val acGenContext = runningExecutionContext
            val acGenerator = acGenContext.generator as SyncGeneratorType? ?: neverHappens()
            val res = yieldAll(result)
            executionContextStack.removeTop()
            acGenerator.state = SyncGeneratorState.COMPLETED
            complete = null // drops the closure since it is no longer needed
            val resValue = when (res) {
                is Completion.WideNormal -> NullType
                is Completion.Return -> res.value
                is Completion.Abrupt -> return@f res
            }
            createIteratorResult(resValue, true)
                .toNormal()
        }
        // TODO: step 5
        context = genContext
        state = SyncGeneratorState.SUSPENDED_START
    }
    /**
     * Note that `executionContextStack.removeTop()` is not needed because it will be performed by yield expression.
     *
     * @see syncYield
     */
    fun resume(value: LanguageType?, brand: String? = null): NonEmptyOrAbrupt {
        validate(brand)
            .orReturn { return it }
        if (state == SyncGeneratorState.COMPLETED) return createIteratorResult(NullType, true).toNormal()
        assert(state.isOneOf(SyncGeneratorState.SUSPENDED_START, SyncGeneratorState.SUSPENDED_YIELD))
        state = SyncGeneratorState.EXECUTING
        executionContextStack.addTop(context)
        val res = complete!!.next(value.normalizeToNormal())
        return res
    }
    @EsSpec("GeneratorValidate")
    fun validate(brand: String?): EmptyOrAbrupt {
        if (this.brand != brand) return throwError(TypeErrorKind.INCOMPATIBLE_METHOD_RECEIVER, TODO(), TODO())
        if (state == SyncGeneratorState.EXECUTING) return throwError(TypeErrorKind.GENERATOR_RUNNING)
        return empty
    }
}
