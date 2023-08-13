package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal

@EsSpec("GetMethod")
internal fun LanguageType.getMethod(key: PropertyKey): MaybeAbrupt<FunctionType>? {
    val func = getProperty(key)
        .orReturn { return it }
        .normalizeNull()
        ?.requireToBe<FunctionType> { return it }
        ?: return null
    return func.toNormal()
}
