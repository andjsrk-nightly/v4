package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("GetV")
internal fun LanguageType.getProperty(key: PropertyKey): Completion {
    if (this is ObjectType) return get(key) // calls a method that is implemented on concrete type
    TODO()
}
