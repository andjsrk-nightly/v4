package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Generator
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens

class SyncGeneratorType(
    override val brand: String? = null,
): GeneratorType<SyncGeneratorState>(lazy { Generator.instancePrototype }) {
    override var context = runningExecutionContext
    override var state: SyncGeneratorState? = null // [[GeneratorState]]
    var complete: (() -> NonEmptyNormalOrAbrupt)? = null
    @EsSpec("GeneratorStart")
    override fun start(createResult: () -> NormalOrAbrupt) {
        val genContext = runningExecutionContext
        genContext.generator = this
        complete = complete@ { // TODO: migrate to iterators
            val acGenContext = runningExecutionContext
            val acGenerator = acGenContext.generator as SyncGeneratorType? ?: neverHappens()
            val result = createResult()
            executionContextStack.removeTop()
            acGenerator.state = SyncGeneratorState.COMPLETED
            complete = null // drops the closure since it is no longer needed
            val resValue = when (result) {
                is Completion.WideNormal -> NullType
                is Completion.Return -> result.value
                is Completion.Abrupt -> return@complete result
            }
            createIteratorResult(resValue, true)
                .toNormal()
        }
        // TODO: step 5
        context = genContext
        state = SyncGeneratorState.SUSPENDED_START
    }
}
