package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("GetMethod")
internal fun LanguageType.getMethod(key: PropertyKey): MaybeAbrupt<FunctionType>? {
    val func = getProperty(key)
        .returnIfAbrupt { return it }
        .normalizeNull()
        ?.requireToBe<FunctionType> { return it }
        ?: return null
    return Completion.Normal(func)
}
