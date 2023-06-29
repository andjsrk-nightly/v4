package io.github.andjsrk.v4.evaluate.builtin.`object`

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.`object`.static.*
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

@EsSpec("%Object%")
val Object = BuiltinClassType(
    null,
    mutableMapOf(
        "assignEnumerableProperties".languageValue to DataProperty.sealed(assignEnumerableProperties),
        "create".languageValue to DataProperty.sealed(create),
        "entries".languageValue to DataProperty.sealed(entries),
        "freeze".languageValue to DataProperty.sealed(freeze),
        "getOwnEnumerableStringKeys".languageValue to DataProperty.sealed(getOwnEnumerableStringKeys),
        "getOwnEnumerableStringKeyValues".languageValue to DataProperty.sealed(getOwnEnumerableStringKeyValues),
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
