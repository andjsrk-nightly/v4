package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.evaluate.type.*

internal const val REGEXP_NOT_SUPPORTED_YET = "Regular expressions are not supported yet."

val RegExp = BuiltinClassType(
    "RegExp",
    Object,
    mutableMapOf(
        // TODO
    ),
    mutableMapOf(
        // TODO
    ),
    { RegExpType() },
    constructor { _, _ ->
        TODO(REGEXP_NOT_SUPPORTED_YET)
    },
)
