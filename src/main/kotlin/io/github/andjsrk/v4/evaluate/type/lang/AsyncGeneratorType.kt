package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.AsyncGeneratorState
import io.github.andjsrk.v4.evaluate.builtin.AsyncGenerator
import io.github.andjsrk.v4.evaluate.runningExecutionContext
import io.github.andjsrk.v4.evaluate.type.NormalOrAbrupt

class AsyncGeneratorType(
    override val brand: String? = null,
): GeneratorType<AsyncGeneratorState>(lazy { AsyncGenerator.instancePrototype }) {
    override var context = runningExecutionContext
    override var state: AsyncGeneratorState? = null // [[AsyncGeneratorState]]
    @EsSpec("AsyncGeneratorStart")
    override fun start(result: Sequence<NormalOrAbrupt>) {
        TODO()
    }
}
