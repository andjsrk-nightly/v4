package io.github.andjsrk.v4.evaluate.builtin.number

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.builtin.number.static.*
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.builtin.sealedData
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

@EsSpec("%Number%")
val Number = BuiltinClassType(
    "Number",
    Object,
    mutableMapOf(
        "EPSILON".sealedData(NumberType.EPSILON.languageValue),
        "MAX_SAFE_INTEGER".sealedData(NumberType.MAX_SAFE_INTEGER.languageValue),
        "MAX_VALUE".sealedData(NumberType.MAX_VALUE.languageValue),
        "MIN_SAFE_INTEGER".sealedData(NumberType.MIN_SAFE_INTEGER.languageValue),
        "MIN_VALUE".sealedData(NumberType.MIN_VALUE.languageValue),
        "NaN".sealedData(NumberType.NaN),
        "NEGATIVE_INFINITY".sealedData(NumberType.NEGATIVE_INFINITY),
        "POSITIVE_INFINITY".sealedData(NumberType.POSITIVE_INFINITY),
        sealedData(::from),
        sealedData(::isFinite),
        sealedData(::isInteger),
        sealedData(::isNaN),
        sealedData(::isSafeInteger),
        sealedData(::parseLeadingDecimal),
        sealedData(::parseLeadingInteger),
        // TODO
    ),
    mutableMapOf(
        SymbolType.WellKnown.toString.sealedData(toString),
        sealedData(::toRadix),
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "Number")
    },
)
