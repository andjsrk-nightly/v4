package io.github.andjsrk.v4.evaluate.builtin.boolean.static

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.not
import java.math.BigInteger

val from = BuiltinFunctionType("from", 1u) fn@ { _, args ->
    val value = args[0]
    Completion.Normal(
        when (value) {
            NullType -> false
            is StringType -> value.value.isNotEmpty()
            is NumberType -> value.not { isZero } && value.not { isNaN }
            is BooleanType ->  return@fn Completion.Normal(value)
            is BigIntType -> value.value != BigInteger.ZERO
            is SymbolType -> return@fn Completion.Throw(NullType/* TypeError */)
            is ObjectType -> return@fn Completion.Throw(NullType/* TypeError */)
        }
            .languageValue
    )
}
