package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.*

sealed interface PropertyKey: EvaluationResult

/**
 * Returns string representation of the key.
 */
fun PropertyKey.string() =
    when (this) {
        is PrivateName -> description
        is LanguageTypePropertyKey -> stringify(this)
            .unwrap()
            .nativeValue
    }
fun PropertyKey.toLanguageValue(): LanguageType =
    when (this) {
        is PrivateName -> description.languageValue
        is LanguageTypePropertyKey -> this
    }
