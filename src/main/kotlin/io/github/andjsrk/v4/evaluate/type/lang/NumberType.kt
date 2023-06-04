package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.not
import kotlin.math.*

internal data class NumberType(
    override val value: Double,
    val isNegative: Boolean = value.isNegative
): NumericType<NumberType> {
    inline val isZero get() =
        value == 0.0
    inline val isInfinity get() =
        this === POSITIVE_INFINITY || this === NEGATIVE_INFINITY
    inline val isFinite get() =
        not { this.isInfinity || this === NaN }
    override fun unaryMinus() =
        when (this) {
            NaN -> this
            POSITIVE_INFINITY -> NEGATIVE_INFINITY
            NEGATIVE_INFINITY -> POSITIVE_INFINITY
            POSITIVE_ZERO -> NEGATIVE_ZERO
            NEGATIVE_ZERO -> POSITIVE_ZERO
            else -> NumberType(-value)
        }
    override fun bitwiseNot() =
        NumberType(value.toInt().inv().toDouble())
    override fun pow(other: NumberType): NumberType =
        when {
            other === NaN -> other
            other.isZero -> NumberType(1.0)
            this === NaN -> this
            this.isInfinity ->
                if (isNegative) {
                    if (other.value > 0) POSITIVE_INFINITY
                    else POSITIVE_ZERO
                } else {
                    if (other.value > 0) {
                        if (other.value.isOddInteger) NEGATIVE_INFINITY
                        else POSITIVE_INFINITY
                    } else {
                        if (other.value.isOddInteger) NEGATIVE_ZERO
                        else POSITIVE_ZERO
                    }
                }
            this == POSITIVE_ZERO ->
                if (other.value > 0) POSITIVE_ZERO
                else POSITIVE_INFINITY
            this == NEGATIVE_ZERO ->
                if (other.value > 0) {
                    if (other.value.isOddInteger) NEGATIVE_ZERO
                    else POSITIVE_ZERO
                } else {
                    if (other.value.isOddInteger) NEGATIVE_INFINITY
                    else POSITIVE_INFINITY
                }
            other == POSITIVE_INFINITY -> {
                val absBase = abs(value)
                when {
                    absBase > 1 -> POSITIVE_INFINITY
                    absBase < 1 -> POSITIVE_ZERO
                    else -> NaN
                }
            }
            other == NEGATIVE_INFINITY -> {
                val absBase = abs(value)
                when {
                    absBase > 1 -> POSITIVE_ZERO
                    absBase < 1 -> POSITIVE_INFINITY
                    else -> NaN
                }
            }
            value < 0 && other.value.not { isInteger } -> NaN
            else -> NumberType(value.pow(other.value))
        }
    override fun times(other: NumberType): NumberType =
        when {
            this === NaN || other === NaN -> this
            this.isInfinity -> when {
                other.value == 0.0 -> NumberType(Double.NaN)
                other.value > 0 -> this
                else -> -this
            }
            other.isInfinity -> when {
                this.isZero -> NumberType(Double.NaN)
                value > 0 -> other
                else -> -other
            }
            this === NEGATIVE_ZERO ->
                if (other.isNegative) POSITIVE_ZERO
                else this
            other === NEGATIVE_ZERO ->
                if (this.isNegative) POSITIVE_ZERO
                else other
            else ->
                NumberType(value * other.value)
        }
    override fun plus(other: NumberType) =
        NumberType(value + other.value)
    override fun minus(other: NumberType) =
        NumberType(value - other.value)
    override fun lessThan(other: NumberType, undefinedReplacement: BooleanType) =
        when {
            this == NaN || other == NaN -> undefinedReplacement
            this.value == other.value -> BooleanType.FALSE // +0 < -0 or -0 < +0 will be handled on this case
            this == POSITIVE_INFINITY -> BooleanType.FALSE
            other == POSITIVE_INFINITY -> BooleanType.TRUE
            other == NEGATIVE_INFINITY -> BooleanType.FALSE
            this == NEGATIVE_INFINITY -> BooleanType.TRUE
            else -> BooleanType.from(value < other.value)
        }
    /**
     * Note that the function assumes that the number is an integer if [radix] is not `10`.
     */
    fun toString(radix: UInt): StringType =
        StringType(when {
            this === NaN -> "NaN"
            this.isZero -> "0"
            value < 0 -> "-${(-this).toString(radix).value}"
            this.isInfinity -> "Infinity"
            else -> when (radix) {
                10u ->
                    value
                        .toBigDecimal().toPlainString() // prevent scientific notation
                        .removeSuffix(".0") // remove trailing `.0` if possible because trailing `.0` means the number can be represented as an integer
                else -> {
                    assert(value.isInteger)
                    value.toBigDecimal().toBigIntegerExact().toString(radix.toInt())
                }
            }
        })

    companion object {
        val POSITIVE_INFINITY = NumberType(Double.POSITIVE_INFINITY)
        val NEGATIVE_INFINITY = NumberType(Double.NEGATIVE_INFINITY)
        val POSITIVE_ZERO = NumberType(0.0)
        val NEGATIVE_ZERO = NumberType(-0.0)
        val NaN = NumberType(Double.NaN)
    }
}

private val Double.isNegative get() =
    1.0.withSign(this) < 0

private inline val Double.isInteger get() =
    this == truncate(this)
private inline val Int.isOdd get() =
    this and 1 != 0
private inline val Double.isOddInteger get() =
    this.isInteger && this.toInt().isOdd
