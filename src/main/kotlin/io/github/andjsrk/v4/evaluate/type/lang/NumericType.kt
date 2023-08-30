package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt

sealed interface NumericType<out Self: NumericType<Self>>: PrimitiveLanguageType {
    @EsSpec("::unaryMinus")
    operator fun unaryMinus(): Self

    @EsSpec("::bitwiseNOT")
    fun bitwiseNot(): MaybeAbrupt<Self>

    @EsSpec("::exponentiate")
    fun pow(other: @UnsafeVariance Self): MaybeAbrupt<Self>

    @EsSpec("::multiply")
    operator fun times(other: @UnsafeVariance Self): Self

    @EsSpec("::divide")
    operator fun div(other: @UnsafeVariance Self): MaybeAbrupt<Self>

    @EsSpec("::remainder")
    operator fun rem(other: @UnsafeVariance Self): MaybeAbrupt<Self>

    // the method is written as an experiment about if the method calms false positive of Kotlin compiler
    fun mod(other: @UnsafeVariance Self): MaybeAbrupt<Self> =
        rem(other)

    @EsSpec("::add")
    operator fun plus(other: @UnsafeVariance Self): Self

    @EsSpec("::subtract")
    operator fun minus(other: @UnsafeVariance Self): Self

    @EsSpec("::leftShift")
    fun leftShift(other: @UnsafeVariance Self): MaybeAbrupt<Self>

    @EsSpec("::signedRightShift")
    fun signedRightShift(other: @UnsafeVariance Self): MaybeAbrupt<Self>

    @EsSpec("::unsignedRightShift")
    fun unsignedRightShift(other: @UnsafeVariance  Self): MaybeAbrupt<Self>

    @EsSpec("::lessThan")
    fun lessThan(other: @UnsafeVariance Self): MaybeAbrupt<BooleanType>

    @EsSpec("::equal")
    fun equal(other: @UnsafeVariance Self): Boolean

    @EsSpec("::bitwiseAND")
    fun bitwiseAnd(other: @UnsafeVariance Self): MaybeAbrupt<Self>

    @EsSpec("::bitwiseXOR")
    fun bitwiseXor(other: @UnsafeVariance Self): MaybeAbrupt<Self>

    @EsSpec("::bitwiseOR")
    fun bitwiseOr(other: @UnsafeVariance Self): MaybeAbrupt<Self>

    @EsSpec("::toString")
    fun toString(radix: Int): StringType
}
