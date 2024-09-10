package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.builtin.*
import io.github.andjsrk.v4.evaluate.builtin.Boolean
import io.github.andjsrk.v4.evaluate.builtin.String
import io.github.andjsrk.v4.evaluate.type.*

internal val PrimitiveLanguageType.associatedClass get() =
    when (this) {
        NullType -> null
        is StringType -> String
        is NumberType -> Number
        is BigIntType -> BigInt
        is BooleanType -> Boolean
        is SymbolType -> Symbol
    }
