package io.github.andjsrk.v4.evaluate.builtin.bigint.static

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
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
                if (rest.any { it.not { isDecimalDigit } }) return@fn throwError(TypeErrorKind.BIGINT_FROM_INVALID_VALUE, value.value)
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
            is ObjectType -> return@fn throwError(TypeErrorKind.BIGINT_FROM_INVALID_VALUE, generalizedDescriptionOf(value))
            is SymbolType -> return@fn throwError(TypeErrorKind.BIGINT_FROM_INVALID_VALUE, generalizedDescriptionOf(value))
        }
    )
}

private fun NumberType.toBigInt(): MaybeAbrupt<BigIntType> {
    if (this.not { isInteger }) return throwError(TypeErrorKind.BIGINT_FROM_NON_INTEGER, toString(10).value)
    return Completion.Normal(
        value.toBigDecimal().toBigIntegerExact()
            .languageValue
    )
}
