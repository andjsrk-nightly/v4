package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import java.math.BigInteger

@EsSpec("BigInt(value)") // (as a normal function)
private val bigintFrom = BuiltinFunctionType("from", 1u) fn@ { _, args ->
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

private val bigintToString = builtinMethod(SymbolType.WellKnown.toString) fn@ { thisArg, args ->
    val bigint = thisArg.requireToBe<BigIntType> { return@fn it }
    val radix = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRadix { return@fn it }
        ?: 10
    Completion.Normal(
        bigint.toString(radix)
    )
}

val BigInt = BuiltinClassType(
    "BigInt",
    Object,
    mutableMapOf(
        "from".sealedData(bigintFrom),
        // TODO
    ),
    mutableMapOf(
        SymbolType.WellKnown.toString.sealedData(bigintToString),
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "BigInt")
    },
)

private fun NumberType.toBigInt(): MaybeAbrupt<BigIntType> {
    if (this.not { isInteger }) return throwError(TypeErrorKind.BIGINT_FROM_NON_INTEGER, toString(10).value)
    return Completion.Normal(
        value.toBigDecimal().toBigIntegerExact()
            .languageValue
    )
}
