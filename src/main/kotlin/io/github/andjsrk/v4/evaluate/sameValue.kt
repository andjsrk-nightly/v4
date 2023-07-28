package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NumberType

@EsSpec("SameValue")
@EsSpec("SameValueZero") // covers it by passing `NumberType::internallyStrictlyEqual` as comparator
internal fun sameValue(
    left: LanguageType,
    right: LanguageType,
    comparator: (left: NumberType, right: NumberType) -> Boolean = NumberType::internallyStrictlyEqual,
) =
    when {
        left::class != right::class -> false
        left is NumberType -> comparator(left, right as NumberType)
        else -> equal(left, right)
    }
