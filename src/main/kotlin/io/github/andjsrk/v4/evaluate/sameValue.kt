package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType.Companion.FALSE
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NumberType

@EsSpec("SameValue")
internal fun sameValue(left: LanguageType, right: LanguageType) =
    when {
        left::class != right::class -> FALSE
        left is NumberType -> left.internallyStrictlyEqual(right as NumberType)
        else -> equal(left, right)
    }
