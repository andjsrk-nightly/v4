package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

internal fun createIteratorResult(value: LanguageType, done: Boolean) =
    ObjectType.createNormal().apply {
        createDataProperty("value".languageValue, value)
        createDataProperty("done".languageValue, done.languageValue)
    }
