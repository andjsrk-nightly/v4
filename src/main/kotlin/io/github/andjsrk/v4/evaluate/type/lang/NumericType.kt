package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType

internal sealed interface NumericType<Self: NumericType<Self>>: LanguageType {
    @EsSpec("::unaryMinus")
    operator fun unaryMinus(): Self
    @EsSpec("::bitwiseNOT")
    fun bitwiseNot(): Self
    @EsSpec("::exponentiate")
    fun pow(other: Self): AbstractType/* return type can be a completion in BigInt::exponentiate */
    @EsSpec("::multiply")
    operator fun times(other: Self): Self
    @EsSpec("::add")
    operator fun plus(other: Self): Self
    @EsSpec("::subtract")
    operator fun minus(other: Self): Self
    @EsSpec("::lessThan")
    fun lessThan(other: Self, undefinedReplacement: BooleanType): BooleanType
}
