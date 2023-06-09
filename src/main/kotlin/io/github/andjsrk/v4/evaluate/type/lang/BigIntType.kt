package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import java.math.BigInteger

@JvmInline
value class BigIntType(override val value: BigInteger): NumericType<BigIntType> {
    override fun unaryMinus() =
        BigIntType(-value)
    override fun bitwiseNot() =
        BigIntType(-value - BigInteger.ONE)
    override fun pow(other: BigIntType) =
        when {
            other.value < BigInteger.ZERO -> throwError(RangeErrorKind.MUST_BE_POSITIVE)
            else -> Completion.Normal(BigIntType(value.operate(BigInteger::pow, other.value)))
        }
    override fun times(other: BigIntType) =
        BigIntType(value * other.value)
    override fun div(other: BigIntType): MaybeAbrupt<BigIntType> {
        if (other.value == BigInteger.ZERO) return throwError(RangeErrorKind.BIGINT_DIV_ZERO)
        return Completion.Normal(BigIntType(value / other.value))
    }
    override fun rem(other: BigIntType): MaybeAbrupt<BigIntType> {
        if (other.value == BigInteger.ZERO) return throwError(RangeErrorKind.BIGINT_DIV_ZERO)
        return Completion.Normal(BigIntType(value % other.value))
    }
    override fun plus(other: BigIntType) =
        BigIntType(value + other.value)
    override fun minus(other: BigIntType) =
        BigIntType(value - other.value)
    override fun leftShift(other: BigIntType) =
        BigIntType(value.operate(BigInteger::shiftLeft, other.value))
    override fun signedRightShift(other: BigIntType) =
        leftShift(-other)
    override fun unsignedRightShift(other: BigIntType) =
        throwError(TypeErrorKind.BIGINT_SHR)
    override fun lessThan(other: BigIntType, undefinedReplacement: BooleanType): BooleanType =
        BooleanType.from(value < other.value)
    override fun equal(other: BigIntType) =
        BooleanType.from(value == other.value)
    override fun bitwiseAnd(other: BigIntType) =
        BigIntType(value and other.value)
    override fun bitwiseXor(other: BigIntType) =
        BigIntType(value xor other.value)
    override fun bitwiseOr(other: BigIntType) =
        BigIntType(value or other.value)
    override fun toString(radix: Int) =
        value.toString(radix).languageValue
}

private fun BigInteger.operate(operation: BigInteger.(Int) -> BigInteger, other: BigInteger): BigInteger {
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
