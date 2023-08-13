package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.evaluate.stringify
import io.github.andjsrk.v4.evaluate.unwrap

sealed interface PropertyKey: PrimitiveLanguageType {
    /**
     * Returns string representation of the key.
     */
    fun string() =
        stringify(this)
            .unwrap()
            .value
}
