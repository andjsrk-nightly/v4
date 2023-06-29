package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

@EsSpec("%String%")
val String = BuiltinClassType(
    Object,
    mutableMapOf(
        // TODO
    ),
    mutableMapOf(
        // TODO
    ),
    constructor {
        TODO()
    },
)
