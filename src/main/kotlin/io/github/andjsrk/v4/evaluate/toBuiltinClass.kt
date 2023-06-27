package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.builtin.number.Number
import io.github.andjsrk.v4.evaluate.builtin.string.String
import io.github.andjsrk.v4.evaluate.builtin.symbol.Symbol
import io.github.andjsrk.v4.evaluate.type.lang.*

internal fun PrimitiveLanguageType.toBuiltinClass() =
    when (this) {
        is StringType -> String
        is NumberType -> Number
        is SymbolType -> Symbol
        else -> TODO()
    }
