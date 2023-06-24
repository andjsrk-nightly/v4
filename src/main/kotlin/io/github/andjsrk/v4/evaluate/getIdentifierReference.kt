package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.StringType

@EsSpec("GetIdentifierReference")
internal fun getIdentifierReference(env: Environment?, name: StringType): Completion.WideNormal<Reference> {
    if (env == null) return Completion.WideNormal(Reference(null, name))

    if (env.hasBinding(name.value)) return Completion.WideNormal(Reference(env, name))
    else return getIdentifierReference(env.outer, name)
}
