package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.CompilerFalsePositive
import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*

@EsSpec("ResolveBinding")
fun resolveBinding(name: StringType, env: Environment? = null): @CompilerFalsePositive MaybeThrow<Reference> =
    getIdentifierReference(env ?: runningExecutionContext.lexicalEnvNotNull, name)

@EsSpec("ResolvePrivateIdentifier")
fun resolvePrivateIdentifier(name: String, env: PrivateEnvironment): PrivateName =
    env.names.firstOrNull { it.description == name }
        ?: resolvePrivateIdentifier(name, env.outer!!)
