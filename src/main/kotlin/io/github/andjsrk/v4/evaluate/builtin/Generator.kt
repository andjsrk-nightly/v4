package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

@EsSpec("%GeneratorFunction.prototype%")
val Generator = BuiltinClassType(
    "Generator",
    Object,
    mutableMapOf(),
    mutableMapOf(),
    constructor { _, _ ->
        TODO()
    },
)
