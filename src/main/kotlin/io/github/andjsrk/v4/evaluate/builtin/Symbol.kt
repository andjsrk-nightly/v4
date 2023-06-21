package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.ClassType
import io.github.andjsrk.v4.evaluate.type.lang.SymbolType

val Symbol = ClassType(
    Object,
    mutableMapOf(
        "toString".languageValue to DataProperty.sealed(SymbolType()),
        // TODO
    ),
) {
    // TODO
}
