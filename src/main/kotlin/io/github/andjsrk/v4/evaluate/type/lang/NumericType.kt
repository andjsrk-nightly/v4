package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec

sealed interface NumericType<Self: NumericType<Self>>: LanguageType {
    @EsSpec("::unaryMinus")
    operator fun unaryMinus(): Self
    @EsSpec("::bitwiseNOT")
    fun bitwiseNot(): Self
    @EsSpec("::add")
    operator fun plus(other: Self): Self
    @EsSpec("::subtract")
    operator fun minus(other: Self): Self
    @EsSpec("::multiply")
    operator fun times(other: Self): Self
}
