package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType.Companion.FALSE
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType.Companion.TRUE
import kotlin.math.*

@JvmInline
value class NumberType(
    override val value: Double
): NumericType<NumberType> {
    internal inline val isNegative get() =
        value.isNegative
    internal inline val isPositive get() =
        !isNegative
    inline val isNaN get() =
        this == NaN
    inline val isZero get() =
        value == 0.0
    inline val isPositiveInfinity get() =
        this == POSITIVE_INFINITY
    inline val isNegativeInfinity get() =
        this == NEGATIVE_INFINITY
    inline val isInfinity get() =
        this.isPositiveInfinity || this.isNegativeInfinity
    @EsSpec("finite")
    inline val isFinite get() =
        not { this.isInfinity || this.isNaN }
    override fun unaryMinus() =
        when (this) {
            NaN -> this
            POSITIVE_INFINITY -> NEGATIVE_INFINITY
            NEGATIVE_INFINITY -> POSITIVE_INFINITY
            POSITIVE_ZERO -> NEGATIVE_ZERO
            NEGATIVE_ZERO -> POSITIVE_ZERO
            else -> NumberType(-value)
        }
    override fun bitwiseNot(): MaybeAbrupt<NumberType> {
        val value = returnIfAbrupt(this.toInt32()) { return it }
            .value.toInt()
        return Completion.Normal(NumberType(value.inv().toDouble()))
    }
    override fun pow(other: NumberType): NumberType =
        when {
            other.isNaN -> other
            other.isZero -> NumberType(1.0)
            this.isNaN -> this
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
            other.isPositiveInfinity -> {
                val absBase = abs(value)
                when {
                    absBase > 1 -> POSITIVE_INFINITY
                    absBase < 1 -> POSITIVE_ZERO
                    else -> NaN
                }
            }
            other.isNegativeInfinity -> {
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
            this.isNaN || other.isNaN -> this
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
            this == NEGATIVE_ZERO ->
                if (other.isNegative) POSITIVE_ZERO
                else this
            other == NEGATIVE_ZERO ->
                if (this.isNegative) POSITIVE_ZERO
                else other
            else ->
                NumberType(value * other.value)
        }
    override fun div(other: NumberType) =
        when {
            this.isNaN || other.isNaN -> NaN
            this.isInfinity -> when {
                other.isInfinity -> NaN
                other.isPositive -> this
                else -> -this
            }
            other.isPositiveInfinity ->
                if (this.isPositive) POSITIVE_ZERO
                else NEGATIVE_ZERO
            other.isNegativeInfinity ->
                if (this.isPositive) NEGATIVE_ZERO
                else POSITIVE_ZERO
            this.isZero ->
                when {
                    other.isZero -> NaN
                    other.isPositive -> this
                    else -> -this
                }
            other == POSITIVE_ZERO ->
                if (this.isPositive) POSITIVE_INFINITY
                else NEGATIVE_INFINITY
            other == NEGATIVE_ZERO ->
                if (this.isPositive) NEGATIVE_INFINITY
                else POSITIVE_INFINITY
            else -> NumberType(value / other.value)
        }
    override fun rem(other: NumberType) =
        when {
            this.isNaN || other.isNaN -> NaN
            this.isInfinity -> NaN
            other.isInfinity -> this
            other.isZero -> NaN
            this.isZero -> this
            else -> NumberType(value % other.value)
        }
    override fun plus(other: NumberType) =
        when {
            this.isNaN || other.isNaN -> NaN
            this.isInfinity && other.isInfinity && (this.isNegative xor other.isNegative) -> NaN
            this.isInfinity -> this
            other.isInfinity -> other
            else -> NumberType(value + other.value)
        }
    override fun minus(other: NumberType) =
        this + -other
    private fun <T> generalShift(
        other: NumberType,
        leftCoercion: () -> Completion<NumberType>,
        leftTransform: Double.() -> T,
        operation: (T, Int) -> Double,
    ): MaybeAbrupt<NumberType> {
        val left = returnIfAbrupt(leftCoercion()) { return it }
            .value.leftTransform()
        val right = returnIfAbrupt(other.toUint32()) { return it }
            .value.toInt()
        val shiftCount = right % 32
        val result = operation(left, shiftCount)
        return Completion.Normal(NumberType(result))
    }
    override fun leftShift(other: NumberType) =
        generalShift(other, ::toInt32, Double::toInt) { a, b -> (a shl b).toDouble() }
    override fun signedRightShift(other: NumberType) =
        generalShift(other, ::toInt32, Double::toInt) { a, b -> (a shr b).toDouble() }
    override fun unsignedRightShift(other: NumberType) =
        generalShift(other, ::toUint32, Double::toUInt) { a, b -> (a shr b).toDouble() }
    override fun lessThan(other: NumberType, undefinedReplacement: BooleanType) =
        when {
            this.isNaN || other.isNaN -> undefinedReplacement
            this.value == other.value -> FALSE // +0 < -0 or -0 < +0 will be handled on this case
            this.isPositiveInfinity -> FALSE
            other.isPositiveInfinity -> TRUE
            other.isNegativeInfinity -> FALSE
            this.isNegativeInfinity -> TRUE
            else -> BooleanType.from(value < other.value)
        }
    override fun equal(other: NumberType) =
        when {
            this.isNaN || other.isNaN -> FALSE
            this.isZero && other.isZero -> TRUE
            else -> BooleanType.from(this == other)
        }
    @EsSpec("Number::sameValue")
    fun internallyStrictlyEqual(other: NumberType) =
        when {
            this.isNaN && other.isNaN -> TRUE
            this.isZero && other.isZero -> BooleanType.from(this.isNegative == other.isNegative)
            else -> BooleanType.from(this == other)
        }
    @EsSpec("Number::sameValueZero")
    fun internallyEqual(other: NumberType) =
        when {
            this.isNaN && other.isNaN -> TRUE
            this.isZero && other.isZero -> TRUE
            else -> BooleanType.from(this == other)
        }
    @EsSpec("NumberBitwiseOp")
    private fun generalBitwiseOp(other: NumberType, operation: (Int, Int) -> Int): MaybeAbrupt<NumberType> {
        val left = returnIfAbrupt(this.toInt32()) { return it }
            .value.toInt()
        val right = returnIfAbrupt(other.toInt32()) { return it }
            .value.toInt()
        val result = operation(left, right)
        return Completion.Normal(NumberType(result.toDouble()))
    }
    @EsSpec("::bitwiseAND")
    override fun bitwiseAnd(other: NumberType) =
        generalBitwiseOp(other, Int::and)
    @EsSpec("::bitwiseXOR")
    override fun bitwiseXor(other: NumberType) =
        generalBitwiseOp(other, Int::xor)
    @EsSpec("::bitwiseOR")
    override fun bitwiseOr(other: NumberType) =
        generalBitwiseOp(other, Int::or)
    /**
     * Note that the function assumes that the number is an integer if [radix] is not `10`.
     */
    @EsSpec("Number::toString")
    override fun toString(radix: Int): StringType =
        StringType(when {
            this.isNaN -> "NaN"
            this.isZero -> "0"
            value < 0 -> "-${(-this).toString(radix).value}"
            this.isInfinity -> "Infinity"
            else -> when (radix) {
                10 ->
                    value
                        .toBigDecimal().toPlainString() // prevent scientific notation
                        .removeSuffix(".0") // remove trailing `.0` if possible because trailing `.0` means the number can be represented as an integer
                else -> {
                    assert(value.isInteger)
                    value.toBigDecimal().toBigIntegerExact().toString(radix)
                }
            }
        })

    companion object {
        val POSITIVE_INFINITY = NumberType(Double.POSITIVE_INFINITY)
        val NEGATIVE_INFINITY = NumberType(Double.NEGATIVE_INFINITY)
        val POSITIVE_ZERO = NumberType(0.0)
        val NEGATIVE_ZERO = NumberType(-0.0)
        val NaN = NumberType(Double.NaN)

        const val EPSILON = 2.220446049250313E-16
        const val MAX_SAFE_INTEGER = 9007199254740991.0
        const val MAX_VALUE = Double.MAX_VALUE
        const val MIN_SAFE_INTEGER = -MAX_SAFE_INTEGER
        const val MIN_VALUE = Double.MIN_VALUE
    }
}

private val Double.isNegative get() =
    1.0.withSign(this) < 0

private inline val Int.isOdd get() =
    this and 1 != 0
private inline val Double.isOddInteger get() =
    this.isInteger && this.toInt().isOdd
