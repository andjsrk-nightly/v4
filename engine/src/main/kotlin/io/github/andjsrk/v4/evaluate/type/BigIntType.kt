package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import java.math.BigInteger

@JvmInline
value class BigIntType(override val value: BigInteger): NumericType<BigIntType> {
    override fun unaryMinus() =
        (-value)
            .languageValue
    override fun bitwiseNot() =
        (-value - BigInteger.ONE)
            .languageValue
            .toNormal()
    override fun pow(other: BigIntType) =
        when {
            other.value < BigInteger.ZERO -> throwError(RangeErrorKind.NUMBER_MUST_BE, "positive")
            else -> value.operateWithInt(BigInteger::pow, other.value)
                .languageValue
                .toNormal()
        }
    override fun times(other: BigIntType) =
        (value * other.value)
            .languageValue
    override fun div(other: BigIntType): MaybeAbrupt<BigIntType> {
        if (other.value == BigInteger.ZERO) return throwError(RangeErrorKind.BIGINT_DIV_ZERO)
        return (value / other.value)
            .languageValue
            .toNormal()
    }
    override fun rem(other: BigIntType): MaybeAbrupt<BigIntType> =
        if (other.value == BigInteger.ZERO) throwError(RangeErrorKind.BIGINT_DIV_ZERO)
        else (value % other.value)
            .languageValue
            .toNormal()
    override fun plus(other: BigIntType) =
        (value + other.value)
            .languageValue
    override fun minus(other: BigIntType) =
        (value - other.value)
            .languageValue
    override fun leftShift(other: BigIntType) =
        value.operateWithInt(BigInteger::shiftLeft, other.value)
            .languageValue
            .toNormal()
    override fun signedRightShift(other: BigIntType) =
        leftShift(-other)
    override fun unsignedRightShift(other: BigIntType) =
        throwError(TypeErrorKind.BIGINT_SHR)
    override fun lessThan(other: BigIntType): MaybeThrow<BooleanType> =
        (value < other.value)
            .languageValue
            .toNormal()
    override fun equal(other: BigIntType) =
        value == other.value
    override fun bitwiseAnd(other: BigIntType) =
        (value and other.value)
            .languageValue
            .toNormal()
    override fun bitwiseXor(other: BigIntType) =
        (value xor other.value)
            .languageValue
            .toNormal()
    override fun bitwiseOr(other: BigIntType) =
        (value or other.value)
            .languageValue
            .toNormal()
    override fun toString(radix: Int) =
        value.toString(radix)
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
