package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

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
fun commonYield(value: LanguageType) = lazyFlow f@ {
    when (generatorKind) {
        GeneratorKind.ASYNC -> {
            val awaited = yieldAll(await(value))
                .orReturn { return@f it }
            yieldAll(asyncYield(awaited))
        }
        else -> yieldAll(syncYield(createIteratorResult(value, false)))
    }
}

@EsSpec("GeneratorYield")
fun syncYield(iteratorResult: ObjectType) = lazyFlow {
    val generator = runningExecutionContext.generator!!
    require(generator is SyncGeneratorType)
    generator.state = SyncGeneratorState.SUSPENDED_YIELD
    executionContextStack.removeTop()
    yield(iteratorResult.toNormal()) ?: normalNull
}

@EsSpec("AsyncGeneratorYield")
fun asyncYield(value: LanguageType) = lazyFlow f@ {
    val genContext = runningExecutionContext
    val generator = genContext.generator!!
    require(generator is AsyncGeneratorType)
    executionContextStack.removeTop()
    val prevRealm = runningExecutionContext.realm
    executionContextStack.addTop(genContext)
    generator.completeStep(value.toNormal(), false, prevRealm)
    val resumptionValue =
        if (generator.queue.isNotEmpty()) {
            val toYield = generator.queue.first()
            toYield.completion
        } else {
            generator.state = AsyncGeneratorState.SUSPENDED_YIELD
            executionContextStack.removeTop()
            yield(normalNull)!!
        }
    yieldAll(unwrapYieldResumption(resumptionValue))
}

@EsSpec("AsyncGeneratorUnwrapYieldResumption")
private fun unwrapYieldResumption(completion: NonEmptyOrAbrupt) = lazyFlow f@ {
    val value =
        if (completion is Completion.Return) completion.value
        else completion.orReturn { return@f it }
    val awaited = yieldAll(await(value))
        .orReturn { return@f it }
    Completion.Return(awaited)
}
