package io.github.andjsrk.v4.evaluate.builtin.number.static

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.parseNumber

val from = BuiltinFunctionType("from", 1u) { _, args ->
    val value = args[0]
    Completion.Normal(
        when (value) {
            is ObjectType -> return@BuiltinFunctionType Completion.Throw(NullType/* TypeError */)
            is StringType -> parseNumber(value.value)?.languageValue ?: NumberType.NaN
            is NumberType -> value
            is BigIntType -> value.value.toDouble().languageValue
            is BooleanType -> (if (value.value) 1.0 else 0.0).languageValue
            is NullType -> NumberType.POSITIVE_ZERO
            is SymbolType -> return@BuiltinFunctionType Completion.Throw(NullType/* TypeError */)
        }
    )
}
