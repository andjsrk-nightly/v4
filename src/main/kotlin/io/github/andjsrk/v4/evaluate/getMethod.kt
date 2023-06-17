package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey
import io.github.andjsrk.v4.evaluate.type.spec.Completion

@EsSpec("GetMethod")
internal fun LanguageType.getMethod(key: PropertyKey): Completion {
    val func = get(key)
    TODO()
}