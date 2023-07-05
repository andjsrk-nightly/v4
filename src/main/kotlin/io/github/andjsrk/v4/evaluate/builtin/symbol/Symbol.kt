package io.github.andjsrk.v4.evaluate.builtin.symbol

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.builtin.symbol.static.create
import io.github.andjsrk.v4.evaluate.builtin.symbol.static.`for`
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.AccessorProperty
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.evaluate.type.lang.SymbolType

@EsSpec("%Symbol%")
val Symbol = BuiltinClassType(
    "Symbol",
    Object,
    mutableMapOf(
        "iterator".languageValue to DataProperty.sealed(SymbolType.WellKnown.iterator),
        "toString".languageValue to DataProperty.sealed(SymbolType.WellKnown.toString),
        "create".languageValue to DataProperty.sealed(create),
        "for".languageValue to DataProperty.sealed(`for`),
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
