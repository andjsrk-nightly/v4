package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

fun LanguageType.ownPropertyKeys() =
    when (this) {
        is ObjectType -> this._ownPropertyKeys()
        else -> emptyList()
    }
