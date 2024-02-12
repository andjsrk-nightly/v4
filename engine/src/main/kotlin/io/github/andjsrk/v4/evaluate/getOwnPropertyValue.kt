package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.MaybeThrow
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.evaluate.type.lang.*

fun ObjectType.getOwnPropertyValue(key: PropertyKey): MaybeThrow<LanguageType?> {
    val desc = getOwnProperty(key)
        .orReturnThrow { return it }
        ?: return empty
    return desc.getValue(this, key)
}
