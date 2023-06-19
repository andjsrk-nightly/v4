package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.builtin.Number
import io.github.andjsrk.v4.evaluate.builtin.String
import io.github.andjsrk.v4.evaluate.type.lang.*

val LanguageType.prototype get() =
    when (this) {
        is ObjectType -> prototype
        is StringType -> String.instancePrototype
        is NumberType -> Number.instancePrototype
        else -> TODO()
    }
