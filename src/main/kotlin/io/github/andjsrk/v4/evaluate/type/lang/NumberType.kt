package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType.Companion.FALSE
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType.Companion.TRUE
import io.github.andjsrk.v4.not
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
        val value = this.toInt32()
            .orReturn { return it }
            .toInt()
        val result = value.inv().toDouble()
        return result
            .languageValue
            .toNormal()
    }
    override fun pow(other: NumberType) =
        when {
            other.isNaN -> other
            other.isZero -> 1.0.languageValue
            this.isNaN -> this
            this.isInfinity ->
                if (isNegative) {
                    if (other.value > 0) POSITIVE_INFINITY
                    else POSITIVE_ZERO
                } else {
                    if (other.value > 0) {
                        if (other.isOddInteger) NEGATIVE_INFINITY
                        else POSITIVE_INFINITY
                    } else {
                        if (other.isOddInteger) NEGATIVE_ZERO
                        else POSITIVE_ZERO
                    }
                }
            this == POSITIVE_ZERO ->
                if (other.value > 0) POSITIVE_ZERO
                else POSITIVE_INFINITY
            this == NEGATIVE_ZERO ->
                if (other.value > 0) {
                    if (other.isOddInteger) NEGATIVE_ZERO
                    else POSITIVE_ZERO
                } else {
                    if (other.isOddInteger) NEGATIVE_INFINITY
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
            else -> value.pow(other.value).languageValue
        }
            .toNormal()
    override fun times(other: NumberType): NumberType =
        when {
            this.isNaN || other.isNaN -> this
            this.isInfinity -> when {
                other.value == 0.0 -> NaN
                other.value > 0 -> this
                else -> -this
            }
            other.isInfinity -> when {
                this.isZero -> NaN
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
                (value * other.value)
                    .languageValue
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
            else ->
                (value / other.value)
                    .languageValue
        }
            .toNormal()
    override fun rem(other: NumberType) =
        when {
            this.isNaN || other.isNaN -> NaN
            this.isInfinity -> NaN
            other.isInfinity -> this
            other.isZero -> NaN
            this.isZero -> this
            else ->
                (value % other.value)
                    .languageValue
        }
            .toNormal()
    override fun plus(other: NumberType) =
        when {
            this.isNaN || other.isNaN -> NaN
            this.isInfinity && other.isInfinity && (this.isNegative xor other.isNegative) -> NaN
            this.isInfinity -> this
            other.isInfinity -> other
            else ->
                (value + other.value)
                    .languageValue
        }
    override fun minus(other: NumberType) =
        this + -other
    private fun <T> generalShift(
        other: NumberType,
        leftCoercion: () -> Completion<NumberType>,
        leftTransform: Double.() -> T,
        operation: (T, Int) -> Double,
    ): MaybeAbrupt<NumberType> {
        val left = leftCoercion()
            .orReturn { return it }
            .value
            .leftTransform()
        val right = other.toUint32()
            .orReturn { return it }
            .toInt()
        val shiftCount = right % 32
        val result = operation(left, shiftCount)
        return result
            .languageValue
            .toNormal()
    }
    override fun leftShift(other: NumberType) =
        generalShift(other, ::toInt32, Double::toInt) { a, b -> (a shl b).toDouble() }
    override fun signedRightShift(other: NumberType) =
        generalShift(other, ::toInt32, Double::toInt) { a, b -> (a shr b).toDouble() }
    override fun unsignedRightShift(other: NumberType) =
        generalShift(other, ::toUint32, Double::toUInt) { a, b -> (a shr b).toDouble() }
    override fun lessThan(other: NumberType): MaybeAbrupt<BooleanType> {
        return when {
            this.isNaN || other.isNaN -> return throwError(TypeErrorKind.CANNOT_COMPARE_NAN)
            this.value == other.value -> FALSE // +0 < -0 or -0 < +0 will be handled on this case
            this.isPositiveInfinity -> FALSE
            other.isPositiveInfinity -> TRUE
            other.isNegativeInfinity -> FALSE
            this.isNegativeInfinity -> TRUE
            else ->
                (value < other.value)
                    .languageValue
        }
            .toNormal()
    }
    override fun equal(other: NumberType) =
        when {
            this.isNaN || other.isNaN -> false
            this.isZero && other.isZero -> true
            else -> this == other
        }
    @EsSpec("Number::sameValue")
    fun internallyStrictlyEqual(other: NumberType) =
        when {
            this.isNaN && other.isNaN -> true
            this.isZero && other.isZero -> this.isNegative == other.isNegative
            else -> this == other
        }
    @EsSpec("Number::sameValueZero")
    fun internallyEqual(other: NumberType) =
        when {
            this.isNaN && other.isNaN -> true
            this.isZero && other.isZero -> true
            else -> this == other
        }
    @EsSpec("NumberBitwiseOp")
    private fun generalBitwiseOp(other: NumberType, operation: (Int, Int) -> Int): MaybeAbrupt<NumberType> {
        val left = this.toInt32()
            .orReturn { return it }
            .toInt()
        val right = other.toInt32()
            .orReturn { return it }
            .toInt()
        val result = operation(left, right)
        return result
            .languageValue
            .toNormal()
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
     * Note that the function assumes that the number is either integer or not finite if [radix] is not `10`.
     */
    @EsSpec("Number::toString")
    override fun toString(radix: Int): StringType =
        when {
            this.isNaN -> "NaN"
            this.isZero -> "0"
            value < 0 -> "-${(-this).toString(radix).value}"
            this.isInfinity -> "Infinity"
            else -> when (radix) {
                10 ->
                    value
                        .toBigDecimal().toPlainString() // prevent scientific notation
                        .removeSuffix(".0") // remove trailing `.0` because it means the number can be represented as an integer
                else -> {
                    assert(value.isInteger)
                    value
                        .toBigDecimal()
                        .toBigIntegerExact()
                        .toString(radix)
                }
            }
        }
            .languageValue

    companion object {
        val POSITIVE_INFINITY = Double.POSITIVE_INFINITY.languageValue
        val NEGATIVE_INFINITY = Double.NEGATIVE_INFINITY.languageValue
        val POSITIVE_ZERO = 0.0.languageValue
        val NEGATIVE_ZERO = (-0.0).languageValue
        val NaN = Double.NaN.languageValue

        const val EPSILON = 2.220446049250313E-16
        const val MAX_SAFE_INTEGER = 9007199254740991.0
        const val MAX_VALUE = Double.MAX_VALUE
        const val MIN_SAFE_INTEGER = -MAX_SAFE_INTEGER
        const val MIN_VALUE = Double.MIN_VALUE
    }
}

private val Double.isNegative get() =
    1.0.withSign(this) < 0 // since -0.0 is not less than 0.0, copies the sign first
private inline val Int.isOdd get() =
    this and 1 != 0
internal val NumberType.isInteger get() =
    this.isFinite && value.isInteger
private inline val NumberType.isOddInteger get() =
    this.isInteger && value.toInt().isOdd
/**
 * Note that the function coerces the value to range of [Int].
 */
internal inline fun NumberType.toInt() =
    value.toInt()

internal fun NumberType.requireToBeIntegerWithin(range: LongRange): Long? {
    if (this.isInteger) {
        val long = this.value.toLong()
        if (abs(long) <= Int.MAX_VALUE && long in range) return long
    }
    return null
}
internal fun throwMustBeIntegerInRange(description: String, range: LongRange) =
    throwError(
        RangeErrorKind.MUST_BE_INTEGER_IN_RANGE,
        description,
        range.first.languageValue.display(),
        range.last.languageValue.display(),
    )
internal inline fun NumberType.requireToBeIntWithin(range: LongRange, description: String = "The number", rtn: AbruptReturnLambda) =
    requireToBeIntegerWithin(range)
        ?.toInt()
        ?: rtn(throwMustBeIntegerInRange(description, range))
internal inline fun NumberType.requireToBeIntWithin(range: NamedRange, description: String = "The number", rtn: AbruptReturnLambda) =
    requireToBeIntegerWithin(range.range)
        ?.toInt()
        ?: rtn(unexpectedNumberRange(description, range.name))

internal inline fun NumberType.requireToBeUnsignedInt(rtn: AbruptReturnLambda) =
    requireToBeIntWithin(NamedRange.unsignedInteger, rtn=rtn)
internal inline fun NumberType.requireToBeRadix(rtn: AbruptReturnLambda) =
    requireToBeIntWithin(NamedRange.radix, rtn=rtn)
internal inline fun NumberType.requireToBeIndex(rtn: AbruptReturnLambda) =
    requireToBeIntWithin(NamedRange.unsignedInteger, rtn=rtn)
internal inline fun NumberType.requireToBeIndexWithin(size: Int, rtn: AbruptReturnLambda) =
    requireToBeIntWithin(NamedRange.unsignedInteger, rtn=rtn)
        .let {
            it.takeIf { it < size } ?: rtn(invalidIndex(it))
        }
internal inline fun NumberType.requireToBeRelativeIndex(rtn: AbruptReturnLambda) =
    requireToBeIntWithin(NamedRange.relativeIndex, rtn=rtn)
/**
 * Returns `null` if the number is greater than [Int.MAX_VALUE].
 * Note that the function assumes that the number is an index.
 */
internal fun Int.resolveRelativeIndex(length: Int): Int? {
    if (length <= 0) return null // the index can never be valid
    val index = this
    val mightBeOutOfRange = if (index < 0) length + index else index
    return mightBeOutOfRange.takeIf { it in 0 until length }
}
internal inline fun Int.resolveRelativeIndexOrReturn(length: Int, rtn: AbruptReturnLambda) =
    resolveRelativeIndex(length) ?: rtn(invalidRelativeIndex(this))
internal fun invalidIndex(index: Int) =
    throwError(RangeErrorKind.INVALID_INDEX, index.languageValue.display())
internal fun invalidRelativeIndex(index: Int) =
    throwError(RangeErrorKind.INVALID_RELATIVE_INDEX, index.languageValue.display())
