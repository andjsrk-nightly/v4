package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.*

@EsSpec("%AsyncGeneratorFunction%")
val AsyncGeneratorFunction = BuiltinClassType(
    "AsyncGeneratorFunction",
    Function,
    mutableMapOf(),
    mutableMapOf(),
    { BuiltinFunctionType { _, _ -> normalNull } },
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "AsyncGeneratorFunction")
    },
)
