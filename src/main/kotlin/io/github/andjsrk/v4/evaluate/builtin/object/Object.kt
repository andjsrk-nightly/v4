package io.github.andjsrk.v4.evaluate.builtin.`object`

import io.github.andjsrk.v4.evaluate.builtin.`object`.static.`is`
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

val Object = BuiltinClassType(
    null,
    mutableMapOf(
        "is".languageValue to DataProperty.sealed(`is`),
        // TODO
    ),
    mutableMapOf(
        "run".languageValue to DataProperty.sealed(run),
    ),
    constructor { _ ->
        Completion.Normal(ObjectType.createNormal())
    },
)
