package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

@EsSpec("%AsyncGeneratorFunction.prototype%")
val AsyncGenerator = BuiltinClassType(
    "AsyncGenerator",
    Object,
    mutableMapOf(),
    mutableMapOf(),
    constructor { _, _ ->
        TODO()
    },
)
