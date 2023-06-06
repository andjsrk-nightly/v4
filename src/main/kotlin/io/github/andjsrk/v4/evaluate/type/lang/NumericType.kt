package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType

private typealias SelfOrCompletion = AbstractType

internal sealed interface NumericType<out Self: NumericType<Self>>: LanguageType {
    @EsSpec("::unaryMinus")
    operator fun unaryMinus(): Self

    @EsSpec("::bitwiseNOT")
    fun bitwiseNot(): SelfOrCompletion

    @EsSpec("::exponentiate")
    fun pow(other: @UnsafeVariance Self): SelfOrCompletion

    @EsSpec("::multiply")
    operator fun times(other: @UnsafeVariance Self): Self

    @EsSpec("::divide")
    operator fun div(other: @UnsafeVariance Self): SelfOrCompletion

    @EsSpec("::remainder")
    operator fun rem(other: @UnsafeVariance Self): SelfOrCompletion

    @EsSpec("::add")
    operator fun plus(other: @UnsafeVariance Self): Self

    @EsSpec("::subtract")
    operator fun minus(other: @UnsafeVariance Self): Self

    @EsSpec("::leftShift")
    fun leftShift(other: @UnsafeVariance Self): SelfOrCompletion

    @EsSpec("::signedRightShift")
    fun signedRightShift(other: @UnsafeVariance Self): SelfOrCompletion

    @EsSpec("::unsignedRightShift")
    fun unsignedRightShift(other: @UnsafeVariance  Self): SelfOrCompletion

    @EsSpec("::lessThan")
    fun lessThan(other: @UnsafeVariance Self, undefinedReplacement: BooleanType): BooleanType

    @EsSpec("::equal")
    fun equal(other: @UnsafeVariance Self): BooleanType

    @EsSpec("::bitwiseAND")
    fun bitwiseAnd(other: @UnsafeVariance Self): SelfOrCompletion

    @EsSpec("::bitwiseXOR")
    fun bitwiseXor(other: @UnsafeVariance Self): SelfOrCompletion

    @EsSpec("::bitwiseOR")
    fun bitwiseOr(other: @UnsafeVariance Self): SelfOrCompletion

    @EsSpec("::toString")
    fun toString(radix: Int): StringType
}
