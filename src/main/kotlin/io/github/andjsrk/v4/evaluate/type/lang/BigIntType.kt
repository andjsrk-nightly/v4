package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.type.spec.Completion
import java.math.BigInteger

@JvmInline
internal value class BigIntType(override val value: BigInteger): NumericType<BigIntType> {
    override fun unaryMinus() =
        BigIntType(-value)
    override fun bitwiseNot() =
        BigIntType(-value - BigInteger.ONE)
    override fun pow(other: BigIntType) =
        when {
            other.value < BigInteger.ZERO -> Completion.`throw`(NullType/* RangeError */)
            else -> Completion.normal(BigIntType(value.operate(BigInteger::pow, other.value)))
        }
    override fun times(other: BigIntType) =
        BigIntType(value * other.value)
    override fun div(other: BigIntType): Completion {
        if (other.value == BigInteger.ZERO) return Completion.`throw`(NullType/* RangeError */)
        return Completion.normal(BigIntType(value / other.value))
    }
    override fun rem(other: BigIntType): Completion {
        if (other.value == BigInteger.ZERO) return Completion.`throw`(NullType/* RangeError */)
        return Completion.normal(BigIntType(value % other.value))
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
        Completion.`throw`(NullType/* TypeError */)
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
        StringType(value.toString(radix))
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
