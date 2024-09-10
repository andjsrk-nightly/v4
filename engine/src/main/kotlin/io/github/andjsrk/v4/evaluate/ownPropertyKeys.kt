package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.LanguageType
import io.github.andjsrk.v4.evaluate.type.ObjectType
import io.github.andjsrk.v4.evaluate.type._ownPropertyKeys

fun LanguageType.ownPropertyKeys() =
    when (this) {
        is ObjectType -> _ownPropertyKeys()
        else -> emptyList()
    }
