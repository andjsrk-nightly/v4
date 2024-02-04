package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.MaybeThrow
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal

fun LanguageType.hasProperty(key: PropertyKey): MaybeThrow<BooleanType> =
    when (this) {
        NullType -> BooleanType.FALSE.toNormal()
        is ObjectType -> hasProperty(key)
        else -> prototype!!.hasProperty(key)
        //               ^^ every primitive types have its own prototype object, so it cannot be `null`
    }
