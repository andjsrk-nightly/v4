package io.github.andjsrk.v4.evaluate.builtin.bigint

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.builtin.bigint.static.from
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.evaluate.type.lang.SymbolType

val BigInt = BuiltinClassType(
    "BigInt",
    Object,
    mutableMapOf(
        "from".languageValue to DataProperty.sealed(from),
        // TODO
    ),
    mutableMapOf(
        SymbolType.WellKnown.toString to DataProperty.sealed(toString),
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "BigInt")
    },
)
