package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("GetMethod")
internal fun LanguageType.getMethod(key: PropertyKey): MaybeAbrupt<FunctionType?> {
    val func = getProperty(key)
        .returnIfAbrupt { return it }
        .normalizeNull()
        ?.requireToBe<FunctionType> { return it }
    return func?.toNormal() ?: empty
}
