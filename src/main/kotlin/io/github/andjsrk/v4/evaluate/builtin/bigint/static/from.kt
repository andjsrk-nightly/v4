package io.github.andjsrk.v4.evaluate.builtin.bigint.static

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import java.math.BigInteger

@EsSpec("BigInt(value)") // (as a normal function)
val from = BuiltinFunctionType("from", 1u) fn@ { _, args ->
    Completion.Normal(
        when (val value = args[0]) {
            is NumberType -> return@fn value.toBigInt()
            is StringType -> {
                val (rest, isNegative) =
                    if (value.value.startsWith('-')) value.value.substring(1) to true
                    else value.value to false
                if (rest.any { it.not { isDecimalDigit } }) return@fn Completion.Throw(NullType/* SyntaxError */)
                val bigint = BigInteger(rest)
                BigIntType(
                    if (isNegative) -bigint
                    else bigint
                )
            }
            is BooleanType -> BigIntType(
                if (value.value) BigInteger.ONE
                else BigInteger.ZERO
            )
            NullType -> BigInteger.ZERO.languageValue
            is BigIntType -> value
            is ObjectType -> return@fn Completion.Throw(NullType/* TypeError */)
            is SymbolType -> return@fn Completion.Throw(NullType/* TypeError */)
        }
    )
}

private fun NumberType.toBigInt(): MaybeAbrupt<BigIntType> {
    if (this.not { isInteger }) return Completion.Throw(NullType/* TypeError */)
    return Completion.Normal(
        BigIntType(
            value.toBigDecimal().toBigIntegerExact()
        )
    )
}
