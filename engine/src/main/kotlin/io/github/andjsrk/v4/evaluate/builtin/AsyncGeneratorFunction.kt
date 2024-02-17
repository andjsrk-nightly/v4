package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.constructor

@EsSpec("%AsyncGeneratorFunction%")
val AsyncGeneratorFunction = BuiltinClassType(
    "AsyncGeneratorFunction",
    Function,
    mutableMapOf(),
    mutableMapOf(),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "AsyncGeneratorFunction")
    },
)
