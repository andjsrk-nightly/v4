package io.github.andjsrk.v4.evaluate.builtin.`object`

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.`object`.static.*
import io.github.andjsrk.v4.evaluate.builtin.sealedData
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

@EsSpec("%Object%")
val Object = BuiltinClassType(
    "Object",
    null,
    mutableMapOf(
        sealedData(::assignEnumerableProperties),
        sealedData(::create),
        sealedData(::entries),
        sealedData(::freeze),
        sealedData(::getOwnEnumerableStringKeys),
        sealedData(::getOwnEnumerableStringKeyValues),
        sealedData(::`is`),
        // TODO
    ),
    mutableMapOf(
        sealedData(::run),
    ),
    constructor { obj, _ ->
        Completion.Normal(obj)
    },
)
