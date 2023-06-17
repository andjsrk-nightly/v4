package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

data class Binding(
    val isMutable: Boolean,
    var value: LanguageType?,
) {
    val isInitialized get() =
        value != null
}
