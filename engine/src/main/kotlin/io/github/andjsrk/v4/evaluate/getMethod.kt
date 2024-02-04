package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("GetMethod")
fun LanguageType.getMethod(key: PropertyKey): MaybeThrow<FunctionType?> {
    val func = getProperty(key)
        .orReturnThrow { return it }
        .normalizeNull()
        ?.requireToBe<FunctionType> { return it }
        ?: return empty
    return func.toNormal()
}
