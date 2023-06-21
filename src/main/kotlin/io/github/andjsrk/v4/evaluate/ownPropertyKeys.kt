package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.neverHappens

fun LanguageType.ownPropertyKeys() =
    when (this) {
        NullType -> neverHappens()
        is ObjectType -> this._ownPropertyKeys()
        else -> TODO()
    }
