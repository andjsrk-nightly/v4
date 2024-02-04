package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.NonEmptyOrThrow
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("GetV")
fun LanguageType.getProperty(key: PropertyKey): NonEmptyOrThrow =
    when (this) {
        NullType -> throwError(TypeErrorKind.CANNOT_READ_PROPERTY, key.string(), generalizedDescriptionOf(this))
        is ObjectType -> get(key)
        else -> prototype!!.get(key)
        //               ^^ every primitive types have its own prototype object, so it cannot be `null`
    }
