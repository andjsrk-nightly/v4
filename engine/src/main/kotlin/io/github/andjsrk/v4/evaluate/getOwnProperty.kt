package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens

fun LanguageType.getOwnProperty(key: PropertyKey): MaybeThrow<Property?> =
    when (this) {
        NullType -> neverHappens()
        is ObjectType -> _getOwnProperty(key)
        else -> null.toWideNormal()
    }
