package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.StringType

@EsSpec("GetIdentifierReference")
fun getIdentifierReference(env: Environment?, name: StringType): MaybeThrow<Reference> {
    if (env == null) return Reference(null, name).toWideNormal()

    val hasBinding = env.hasBinding(name.value)
        .orReturnThrow { return it }
        .value
    return (
        if (hasBinding) Reference(env, name).toWideNormal()
        else getIdentifierReference(env.outer, name)
    )
}
