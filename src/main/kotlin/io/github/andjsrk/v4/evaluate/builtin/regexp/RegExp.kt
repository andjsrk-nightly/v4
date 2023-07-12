package io.github.andjsrk.v4.evaluate.builtin.regexp

import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

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
    constructor { _, _ ->
        TODO(REGEXP_NOT_SUPPORTED_YET)
    },
)
