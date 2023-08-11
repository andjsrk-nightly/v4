package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.neverAbrupt
import io.github.andjsrk.v4.evaluate.stringify

sealed interface PropertyKey: PrimitiveLanguageType {
    /**
     * Returns string representation of the key.
     */
    fun string() =
        stringify(this)
            .neverAbrupt()
            .value
}
