package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.AbstractType

sealed interface PropertyKey: AbstractType {
    /**
     * Returns string representation of the key.
     */
    fun string() =
        when (this) {
            is PrivateName -> description
            is LanguageTypePropertyKey -> stringify(this)
                .unwrap()
                .value
        }
}

fun PropertyKey.toLanguageValue(): LanguageType =
    when (this) {
        is PrivateName -> description.languageValue
        is LanguageTypePropertyKey -> this
    }
