package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Environment
import io.github.andjsrk.v4.evaluate.type.Reference
import io.github.andjsrk.v4.evaluate.type.lang.StringType

@EsSpec("GetIdentifierReference")
internal fun getIdentifierReference(env: Environment?, name: StringType): Reference {
    if (env == null) return Reference(null, name)

    if (env.hasBinding(name.value)) return Reference(env, name)
    else return getIdentifierReference(env.outer, name)
}
