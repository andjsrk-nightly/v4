package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey

@EsSpec("GetMethod")
internal fun LanguageType.getMethod(key: PropertyKey): NonEmptyNormalOrAbrupt {
    val func = getProperty(key)
    TODO()
}
