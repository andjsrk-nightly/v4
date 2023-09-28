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
    var complete: Sequence<MaybeEmptyOrAbrupt>? = null
    @EsSpec("GeneratorStart")
    override fun start(result: Sequence<MaybeEmptyOrAbrupt>) {
        val genContext = runningExecutionContext
        genContext.generator = this
        complete = sequence {
            val acGenContext = runningExecutionContext
            val acGenerator = acGenContext.generator as SyncGeneratorType? ?: neverHappens()
            val iter = result.iterator()
            var lastComp: MaybeEmptyOrAbrupt = empty
            while (iter.hasNext()) {
                val comp = iter.next()
                lastComp = comp
                yield(comp)
            }
            executionContextStack.removeTop()
            acGenerator.state = SyncGeneratorState.COMPLETED
            complete = null // drops the closure since it is no longer needed
            val resValue = when (lastComp) {
                is Completion.WideNormal -> NullType
                is Completion.Return -> lastComp.value
                is Completion.Abrupt -> {
                    yield(lastComp)
                    return@sequence
                }
            }
            yield(
                createIteratorResult(resValue, true)
                    .toNormal()
            )
        }
        // TODO: step 5
        context = genContext
        state = SyncGeneratorState.SUSPENDED_START
    }
}
