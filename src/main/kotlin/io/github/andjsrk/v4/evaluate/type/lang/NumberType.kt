package io.github.andjsrk.v4.evaluate.type.lang

import kotlin.math.sign
import kotlin.math.truncate

data class NumberType(
    override val value: Double,
    val isNegative: Boolean = value.sign == -1.0
): NumericType<NumberType> {
    inline val isNaN get() =
        value.isNaN()
    inline val isNegativeZero get() =
        isNegative && value == 0.0
    override fun unaryMinus() =
        if (this.isNaN) this
        else NumberType(-value)
    override fun bitwiseNot() =
        NumberType(value.toInt().inv().toDouble())
    override fun plus(other: NumberType) =
        NumberType(value + other.value)
    override fun minus(other: NumberType) =
        NumberType(value - other.value)
    override fun times(other: NumberType) =
        when {
            this.isNaN || other.isNaN -> this
            value.isInfinite() -> when {
                other.value == 0.0 -> NumberType(Double.NaN)
                other.value > 0 -> this
                else -> -this
            }
            other.value.isInfinite() -> when {
                value == 0.0 -> NumberType(Double.NaN)
                value > 0 -> other
                else -> -other
            }
            this.isNegativeZero ->
                if (other.isNegative) NumberType(0.0)
                else this
            other.isNegativeZero ->
                if (this.isNegative) NumberType(0.0)
                else other
            else ->
                NumberType(value * other.value)
        }
    /**
     * Note that the function assumes that the number is an integer if [radix] is not `10`.
     */
    fun toString(radix: UInt): StringType =
        StringType(run {
            if (this.isNaN) return@run "NaN"
            if (value == 0.0) return@run "0"
            if (value < 0) return@run (-this).toString(radix).value
            if (value.isInfinite()) return@run "Infinity"
            if (radix != 10u) assert(value == truncate(value)) // asserts `value` is an integer
            TODO()
        })
}
