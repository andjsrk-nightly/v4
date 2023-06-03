package io.github.andjsrk.v4.evaluate.type.lang

import java.math.BigInteger

@JvmInline
value class BigIntType(override val value: BigInteger): NumericType<BigIntType> {
    override fun unaryMinus() =
        BigIntType(-value)
    override fun bitwiseNot() =
        BigIntType(value.not())
    override fun plus(other: BigIntType) =
        BigIntType(value + other.value)
    override fun minus(other: BigIntType) =
        BigIntType(value - other.value)
    override fun times(other: BigIntType) =
        BigIntType(value * other.value)
}
