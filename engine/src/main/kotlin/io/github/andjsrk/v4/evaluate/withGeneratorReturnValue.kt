package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.toIterableIterator

fun Iterator<NonEmptyOrAbrupt>.withGeneratorReturnValue(value: LanguageType?) =
    lazyFlow {
        yieldAll(this@withGeneratorReturnValue.toIterableIterator())
        value?.toNormal() ?: normalNull
    }
