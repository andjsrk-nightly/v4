package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.neverHappens

fun LanguageType.getOwnProperty(key: PropertyKey) =
    when (this) {
        NullType -> neverHappens()
        is ObjectType -> _getOwnProperty(key)
        else -> TODO()
    }
