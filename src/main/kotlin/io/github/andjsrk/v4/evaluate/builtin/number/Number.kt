package io.github.andjsrk.v4.evaluate.builtin.number

import io.github.andjsrk.v4.evaluate.builtin.number.static.from
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.evaluate.type.lang.NullType

val Number = BuiltinClassType(
    Object,
    mutableMapOf(
        "from".languageValue to DataProperty.sealed(from)
        // TODO
    ),
    mutableMapOf(
        // TODO
    ),
    constructor {
        Completion.Throw(NullType/* TypeError */)
    },
)
