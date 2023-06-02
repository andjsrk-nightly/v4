package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.type.AbstractType

sealed interface LanguageType: AbstractType {
    val value: Any?
}
