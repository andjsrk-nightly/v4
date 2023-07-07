package io.github.andjsrk.v4.evaluate.builtin.boolean

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.builtin.boolean.static.from
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.builtin.sealedData
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.evaluate.type.lang.SymbolType

val Boolean = BuiltinClassType(
    "Boolean",
    Object,
    mutableMapOf(
        "from".sealedData(from),
    ),
    mutableMapOf(
        SymbolType.WellKnown.toString.sealedData(toString)
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "Boolean")
    },
)
