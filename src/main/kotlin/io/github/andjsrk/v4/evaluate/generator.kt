package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.neverHappens

internal const val GENERATOR_FUNCTION_NOT_SUPPORTED_YET = "Generator functions are not supported yet."

enum class GeneratorKind {
    NON_GENERATOR,
    SYNC,
    ASYNC,
}
enum class SyncGeneratorState {
    SUSPENDED_START,
    SUSPENDED_YIELD,
    EXECUTING,
    COMPLETED,
}

@EsSpec("GetGeneratorKind")
internal val generatorKind: GeneratorKind get() {
    val generator = runningExecutionContext.generator ?: return GeneratorKind.NON_GENERATOR

    if (generator is AsyncGeneratorType) return GeneratorKind.ASYNC
    else return GeneratorKind.SYNC
}

@EsSpec("Yield")
internal fun commonYield(value: LanguageType): NonEmptyNormalOrAbrupt {
    val kind = generatorKind
    if (kind == GeneratorKind.ASYNC) TODO()
    return syncYield(createIteratorResult(value, false))
}

@EsSpec("GeneratorYield")
internal fun syncYield(iteratorResult: ObjectType): NonEmptyNormalOrAbrupt {
    val generator = runningExecutionContext.generator ?: neverHappens()
    require(generator is SyncGeneratorType)
    generator.state = SyncGeneratorState.SUSPENDED_YIELD
    executionContextStack.removeTop()
    TODO()
}
