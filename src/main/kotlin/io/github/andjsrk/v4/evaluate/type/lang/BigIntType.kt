package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.type.spec.Completion
import java.math.BigInteger

@JvmInline
internal value class BigIntType(override val value: BigInteger): NumericType<BigIntType> {
    override fun unaryMinus() =
        BigIntType(-value)
    override fun bitwiseNot() =
        BigIntType(value.not())
    override fun pow(other: BigIntType) =
        when {
            other.value < BigInteger.ZERO -> Completion(Completion.Type.THROW, NullType/* RangeError */)
            else -> Completion.normal(BigIntType(value.pow(other.value)))
        }
    override fun times(other: BigIntType) =
        BigIntType(value * other.value)
    override fun plus(other: BigIntType) =
        BigIntType(value + other.value)
    override fun minus(other: BigIntType) =
        BigIntType(value - other.value)
    override fun lessThan(other: BigIntType, undefinedReplacement: BooleanType): BooleanType =
        BooleanType.from(value < other.value)
}

private fun BigInteger.pow(other: BigInteger): BigInteger {
    if (other < BigInteger.ZERO) throw ArithmeticException("Negative exponent")
    TODO()
}
