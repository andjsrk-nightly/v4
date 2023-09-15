package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.*

fun LanguageType.hasProperty(key: PropertyKey) =
    when (this) {
        NullType -> false
        is ObjectType -> hasProperty(key)
        else -> prototype!!.hasProperty(key)
        //               ^^ every primitive types have its own prototype object, so it cannot be `null`
    }
