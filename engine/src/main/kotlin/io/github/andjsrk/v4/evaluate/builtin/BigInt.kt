package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import java.math.BigInteger

@EsSpec("BigInt(value)")
private val bigintFrom = functionWithoutThis("from", 1u) fn@ { args ->
    when (val value = args[0]) {
        is NumberType -> return@fn value.toBigInt()
        is StringType -> {
            val (sign, rest) = getSignAndRest(value.nativeValue)
            if (rest.any { it.not { isDecimalDigit } }) return@fn throwError(TypeErrorKind.BIGINT_FROM_INVALID_VALUE, value.nativeValue)
            val bigint = BigInteger(rest)
            (if (sign < 0) -bigint else bigint)
                .languageValue
        }
        is BooleanType -> (
            if (value.nativeValue) BigInteger.ONE
            else BigInteger.ZERO
        )
            .languageValue
        NullType -> BigInteger.ZERO.languageValue
        is BigIntType -> value
        is ObjectType -> return@fn throwError(TypeErrorKind.BIGINT_FROM_INVALID_VALUE, generalizedDescriptionOf(value))
        is SymbolType -> return@fn throwError(TypeErrorKind.BIGINT_FROM_INVALID_VALUE, generalizedDescriptionOf(value))
    }
        .toNormal()
}

private val bigintAsIntN = method("asIntN", 1u) fn@ { thisArg, args ->
    val bigint = thisArg.requireToBe<BigIntType> { return@fn it }
    val bits = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeUnsignedInt { return@fn it }
    val half = BigInteger.TWO.pow(bits - 1)
    val modRightHand = half.shiftLeft(1)
    val mod = bigint.nativeValue.mod(modRightHand)
    (if (mod >= half) mod - modRightHand else mod)
        .languageValue
        .toNormal()
}

private val bigintAsUintN = method("asUintN", 1u) fn@ { thisArg, args ->
    val bigint = thisArg.requireToBe<BigIntType> { return@fn it }
    val bits = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeUnsignedInt { return@fn it }
    bigint.nativeValue.mod(BigInteger.TWO.pow(bits))
        .languageValue
        .toNormal()
}

@EsSpec("BigInt.prototype.toString")
private val bigintToRadix = method("toRadix", 1u) fn@ { thisArg, args ->
    val bigint = thisArg.requireToBe<BigIntType> { return@fn it }
    val radix = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRadix { return@fn it }
        ?: 10
    bigint.toString(radix)
        .toNormal()
}

@EsSpec("BigInt.prototype.toString") // radix is fixed to 10
private val bigintToString = method(SymbolType.WellKnown.toString) fn@ { thisArg, args ->
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
    ),
    mutableMapOf(
        sealedMethod(bigintAsIntN),
        sealedMethod(bigintAsUintN),
        sealedMethod(bigintToRadix),
        sealedMethod(bigintToString),
    ),
    { ObjectType.Impl() /* dummy object */ },
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "BigInt")
    },
)

private fun NumberType.toBigInt(): MaybeThrow<BigIntType> {
    if (this.not { isInteger }) return throwError(TypeErrorKind.BIGINT_FROM_NON_INTEGER, toString(10).nativeValue)
    return nativeValue.toBigDecimal().toBigIntegerExact()
        .languageValue
        .toNormal()
}
