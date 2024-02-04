package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.StringType

@EsSpec("ResolveBinding")
fun resolveBinding(name: StringType, env: Environment? = null): MaybeThrow<Reference> =
    getIdentifierReference(env ?: runningExecutionContext.lexicalEnvNotNull, name)
