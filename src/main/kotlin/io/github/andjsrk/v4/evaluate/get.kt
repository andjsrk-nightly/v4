package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.Completion

@EsSpec("GetV")
internal fun LanguageType.get(key: PropertyKey): Completion {
    if (this is ObjectType) return get(key) // calls a method that is implemented on concrete type
    TODO()
}
