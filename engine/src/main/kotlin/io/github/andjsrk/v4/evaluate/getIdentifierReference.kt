package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.StringType

@EsSpec("GetIdentifierReference")
fun getIdentifierReference(env: Environment?, name: StringType): MaybeThrow<Reference> {
    if (env == null) return Reference(null, name).toWideNormal()

    val hasBinding = env.hasBinding(name.nativeValue)
        .orReturnThrow { return it }
        .nativeValue
    return (
        if (hasBinding) Reference(env, name).toWideNormal()
        else getIdentifierReference(env.outer, name)
    )
}
