package io.github.andjsrk.v4.evaluate.builtin.boolean

import io.github.andjsrk.v4.evaluate.builtin.boolean.static.from
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.evaluate.type.lang.SymbolType

val Boolean = BuiltinClassType(
    "Boolean",
    Object,
    mutableMapOf(
        "from".languageValue to DataProperty.sealed(from),
    ),
    mutableMapOf(
        SymbolType.WellKnown.toString to DataProperty.sealed(toString)
        // TODO
    ),
    constructor {
        TODO()
    },
)
