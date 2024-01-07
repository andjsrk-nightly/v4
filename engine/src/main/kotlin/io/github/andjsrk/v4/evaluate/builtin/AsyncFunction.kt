package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.constructor

@EsSpec("%AsyncFunction%")
val AsyncFunction = BuiltinClassType(
    "AsyncFunction",
    Function,
    mutableMapOf(),
    mutableMapOf(),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "AsyncFunction")
    },
)
