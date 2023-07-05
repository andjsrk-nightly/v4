package io.github.andjsrk.v4.evaluate.builtin.function

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

val Function = BuiltinClassType(
    "Function",
    Object,
    mutableMapOf(
        // TODO
    ),
    mutableMapOf(
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "Function")
    },
)
