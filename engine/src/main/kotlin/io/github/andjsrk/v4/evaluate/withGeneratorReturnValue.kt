package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

fun Iterator<NonEmptyOrAbrupt>.withGeneratorReturnValue(value: LanguageType?) =
    iterator {
        yieldAll(this@withGeneratorReturnValue)
        yield(value?.toNormal() ?: normalNull)
    }
