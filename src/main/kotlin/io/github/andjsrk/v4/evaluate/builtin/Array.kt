package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

@EsSpec("%Array%")
val Array = BuiltinClassType(
    "Array",
    Object,
    mutableMapOf(
        // TODO
    ),
    mutableMapOf(
        // TODO
    ),
    constructor { arr, args ->
        TODO()
    },
)
