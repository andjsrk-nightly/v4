package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.StringType
import io.github.andjsrk.v4.evaluate.type.spec.*

@EsSpec("ResolveBinding")
fun resolveBinding(name: StringType, env: Environment? = null): Completion {
    val env = env ?: Evaluator.runningExecutionContext.lexicalEnvironment
    return getIdentifierReference(env, name)
}
