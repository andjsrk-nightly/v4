package io.github.andjsrk.v4.evaluate.builtin.bigint.static

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.isDecimalDigit
import io.github.andjsrk.v4.not
import java.math.BigInteger

val from = BuiltinFunctionType("from", 1u) fn@ { _, args ->
    when (val value = args[0]) {
        is NumberType -> value.toBigInt()
        is StringType -> {
            val (rest, isNegative) =
                if (value.value.startsWith('-')) value.value.substring(1) to true
                else value.value to false
            if (rest.any { it.not { isDecimalDigit } }) return@fn Completion.Throw(NullType/* SyntaxError */)
            val bigint = BigInteger(rest)
            Completion.Normal(
                BigIntType(
                    if (isNegative) -bigint
                    else bigint
                )
            )
        }
        is BigIntType -> Completion.Normal(value)
        else -> TODO()
    }
}

private fun NumberType.toBigInt(): MaybeAbrupt<BigIntType> {
    if (this.not { isInteger }) return Completion.Throw(NullType/* TypeError */)
    return Completion.Normal(
        BigIntType(
            value.toBigDecimal().toBigIntegerExact()
        )
    )
}
