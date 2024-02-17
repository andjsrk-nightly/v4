package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.LanguageType
import io.github.andjsrk.v4.evaluate.type.ObjectType

internal fun createIteratorResult(value: LanguageType, done: Boolean) =
    ObjectType.createNormal().apply {
        createDataProperty("value".languageValue, value)
        createDataProperty("done".languageValue, done.languageValue)
    }
