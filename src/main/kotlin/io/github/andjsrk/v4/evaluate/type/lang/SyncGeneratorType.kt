package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Generator
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens

class SyncGeneratorType(
    override val brand: String? = null,
): GeneratorType<SyncGeneratorState>(lazy { Generator.instancePrototype }) {
    override var context = runningExecutionContext
    override var state: SyncGeneratorState? = null // [[GeneratorState]]
    var complete: EvalFlow<LanguageType?>? = null
    @EsSpec("GeneratorStart")
    override fun start(createResult: () -> EvalFlow<LanguageType?>) {
        val genContext = runningExecutionContext
        genContext.generator = this
        complete = EvalFlow {
            val acGenContext = runningExecutionContext
            val acGenerator = acGenContext.generator as SyncGeneratorType? ?: neverHappens()
            val result = yieldAll(createResult()) ?: empty
            executionContextStack.removeTop()
            acGenerator.state = SyncGeneratorState.COMPLETED
            complete = null // drops the closure since it is no longer needed
            val resValue = when (result) {
                is Completion.WideNormal -> NullType
                is Completion.Return -> result.value
                is Completion.Abrupt -> `return`(result)
            }
            createIteratorResult(resValue, true)
                .toNormal()
        }
        context = genContext
        state = SyncGeneratorState.SUSPENDED_START
    }
    @EsSpec("GeneratorValidate")
    fun validate(brand: String?): EmptyOrAbrupt {
        this.requireToBe<SyncGeneratorType> { return it }
        if (this.brand != brand) return throwError(TypeErrorKind.INCOMPATIBLE_METHOD_RECEIVER, TODO(), TODO())
        if (state == SyncGeneratorState.EXECUTING) return throwError(TypeErrorKind.GENERATOR_RUNNING)
        return empty
    }
    @EsSpec("GeneratorResume")
    fun resume(value: LanguageType?, brand: String?): NonEmptyNormalOrAbrupt {
        validate(brand)
            .returnIfAbrupt { return it }
        if (state == SyncGeneratorState.COMPLETED) return createIteratorResult(NullType, true).toNormal()
        val methodContext = runningExecutionContext
        state = SyncGeneratorState.EXECUTING
        executionContextStack.addTop(context)
        val res = complete!!.resume(value?.toNormal() ?: `null`)
        assert(runningExecutionContext == methodContext)
        return res
    }
    @EsSpec("GeneratorResumeAbrupt")
    fun resumeAbrupt(completion: Completion.Abrupt, brand: String?): NonEmptyNormalOrAbrupt {
        validate(brand)
            .returnIfAbrupt { return it }
        if (state == SyncGeneratorState.SUSPENDED_START) {
            state = SyncGeneratorState.COMPLETED
            complete = null // drops the closure since it is no longer needed
        }
        // branches should be separated to each if statements because above if body contains a side effect(updating `state` to `COMPLETED`)
        if (state == SyncGeneratorState.COMPLETED) {
            if (completion is Completion.Return) return createIteratorResult(completion.value, true).toNormal()
            else return completion
        }
        assert(state == SyncGeneratorState.SUSPENDED_YIELD)
        val methodContext = runningExecutionContext
        state = SyncGeneratorState.EXECUTING
        executionContextStack.addTop(context)
        val res = complete!!.resume(completion)
        assert(runningExecutionContext == methodContext)
        return res
    }
}
