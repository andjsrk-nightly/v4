package io.github.andjsrk.v4.evaluate.builtin.symbol

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.builtin.accessor
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.builtin.sealedData
import io.github.andjsrk.v4.evaluate.builtin.symbol.static.create
import io.github.andjsrk.v4.evaluate.builtin.symbol.static.`for`
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.evaluate.type.lang.SymbolType

@EsSpec("%Symbol%")
val Symbol = BuiltinClassType(
    "Symbol",
    Object,
    mutableMapOf(
        sealedData(SymbolType.WellKnown::iterator),
        sealedData(SymbolType.WellKnown::toString),
        sealedData(::create),
        sealedData(::`for`),
        // TODO
    ),
    mutableMapOf(
        "description".accessor(getter=descriptionGetter),
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "Symbol")
    },
)
