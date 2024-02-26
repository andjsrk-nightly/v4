package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import java.math.BigInteger

@JvmInline
value class BigIntType(override val nativeValue: BigInteger): NumericType<BigIntType> {
    override fun unaryMinus() =
        (-nativeValue)
            .languageValue
    override fun bitwiseNot() =
        (-nativeValue - BigInteger.ONE)
            .languageValue
            .toNormal()
    override fun pow(other: BigIntType) =
        when {
            other.nativeValue < BigInteger.ZERO -> throwError(RangeErrorKind.NUMBER_MUST_BE, "positive")
            else -> nativeValue.operateWithInt(BigInteger::pow, other.nativeValue)
                .languageValue
                .toNormal()
        }
    override fun times(other: BigIntType) =
        (nativeValue * other.nativeValue)
            .languageValue
    override fun div(other: BigIntType): MaybeAbrupt<BigIntType> {
        if (other.nativeValue == BigInteger.ZERO) return throwError(RangeErrorKind.BIGINT_DIV_ZERO)
        return (nativeValue / other.nativeValue)
            .languageValue
            .toNormal()
    }
    override fun rem(other: BigIntType): MaybeAbrupt<BigIntType> =
        if (other.nativeValue == BigInteger.ZERO) throwError(RangeErrorKind.BIGINT_DIV_ZERO)
        else (nativeValue % other.nativeValue)
            .languageValue
            .toNormal()
    override fun plus(other: BigIntType) =
        (nativeValue + other.nativeValue)
            .languageValue
    override fun minus(other: BigIntType) =
        (nativeValue - other.nativeValue)
            .languageValue
    override fun leftShift(other: BigIntType) =
        nativeValue.operateWithInt(BigInteger::shiftLeft, other.nativeValue)
            .languageValue
            .toNormal()
    override fun signedRightShift(other: BigIntType) =
        leftShift(-other)
    override fun unsignedRightShift(other: BigIntType) =
        throwError(TypeErrorKind.BIGINT_SHR)
    override fun lessThan(other: BigIntType): MaybeThrow<BooleanType> =
        (nativeValue < other.nativeValue)
            .languageValue
            .toNormal()
    override fun equal(other: BigIntType) =
        nativeValue == other.nativeValue
    override fun bitwiseAnd(other: BigIntType) =
        (nativeValue and other.nativeValue)
            .languageValue
            .toNormal()
    override fun bitwiseXor(other: BigIntType) =
        (nativeValue xor other.nativeValue)
            .languageValue
            .toNormal()
    override fun bitwiseOr(other: BigIntType) =
        (nativeValue or other.nativeValue)
            .languageValue
            .toNormal()
    override fun toString(radix: Int) =
        nativeValue.toString(radix)
            .languageValue

    override fun toString() = display()
}

private fun BigInteger.operateWithInt(operation: BigInteger.(Int) -> BigInteger, other: BigInteger): BigInteger {
    var acc = this
    var i = BigInteger.ZERO
    val (count, rem) = other.divideAndRemainder(Int.MAX_VALUE.toBigInteger())
    while (i < count) {
        acc = acc.operation(Int.MAX_VALUE)
        i++
    }
    if (rem != BigInteger.ZERO) acc = acc.operation(rem.toInt())
    return acc
}
