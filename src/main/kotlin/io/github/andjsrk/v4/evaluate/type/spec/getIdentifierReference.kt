package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.StringType

@EsSpec("GetIdentifierReference")
fun getIdentifierReference(env: Environment?, name: StringType): Completion {
    if (env == null) return Completion.wideNormal(Reference(null, name))

    if (name.value in env) return Completion.wideNormal(Reference(env, name))
    else return getIdentifierReference(env.outer, name)
}
