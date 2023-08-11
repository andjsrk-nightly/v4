package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.evaluate.type.toNormal
import java.math.BigInteger

@EsSpec("BigInt(value)")
private val bigintFrom = BuiltinFunctionType("from", 1u) fn@ { _, args ->
    when (val value = args[0]) {
        is NumberType -> return@fn value.toBigInt()
        is StringType -> {
            val (sign, rest) = getSignAndRest(value.value)
            if (rest.any { it.not { isDecimalDigit } }) return@fn throwError(TypeErrorKind.BIGINT_FROM_INVALID_VALUE, value.value)
            val bigint = BigInteger(rest)
            BigIntType(
                if (sign < 0) -bigint
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
        .toNormal()
}

@EsSpec("BigInt.prototype.toString") // with dynamic radix
private val bigintToRadix = builtinMethod("toRadix", 1u) fn@ { thisArg, args ->
    val bigint = thisArg.requireToBe<BigIntType> { return@fn it }
    val radix = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRadix { return@fn it }
        ?: 10
    bigint.toString(radix)
        .toNormal()
}

@EsSpec("BigInt.prototype.toString") // radix is fixed to 10
private val bigintToString = builtinMethod(SymbolType.WellKnown.toString) fn@ { thisArg, args ->
    val bigint = thisArg.requireToBe<BigIntType> { return@fn it }
    bigint.toString(10)
        .toNormal()
}

@EsSpec("%BigInt%")
val BigInt = BuiltinClassType(
    "BigInt",
    Object,
    mutableMapOf(
        sealedMethod(bigintFrom),
        // TODO
    ),
    mutableMapOf(
        sealedMethod(bigintToRadix),
        sealedMethod(bigintToString),
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "BigInt")
    },
)

private fun NumberType.toBigInt(): MaybeAbrupt<BigIntType> {
    if (this.not { isInteger }) return throwError(TypeErrorKind.BIGINT_FROM_NON_INTEGER, toString(10).value)
    return value.toBigDecimal().toBigIntegerExact()
        .languageValue
        .toNormal()
}
