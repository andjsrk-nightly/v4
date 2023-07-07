package io.github.andjsrk.v4.evaluate.builtin.symbol

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.builtin.sealedData
import io.github.andjsrk.v4.evaluate.builtin.symbol.static.create
import io.github.andjsrk.v4.evaluate.builtin.symbol.static.`for`
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.AccessorProperty
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.evaluate.type.lang.SymbolType

@EsSpec("%Symbol%")
val Symbol = BuiltinClassType(
    "Symbol",
    Object,
    mutableMapOf(
        "iterator".sealedData(SymbolType.WellKnown.iterator),
        "toString".sealedData(SymbolType.WellKnown.toString),
        "create".sealedData(create),
        "for".sealedData(`for`),
        // TODO
    ),
    mutableMapOf(
        "description".languageValue to AccessorProperty(descriptionGetter),
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "Symbol")
    },
)
