package io.github.andjsrk.v4.evaluate.builtin.number

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.number.static.*
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

@EsSpec("%Number%")
val Number = BuiltinClassType(
    "Number",
    Object,
    mutableMapOf(
        "EPSILON".languageValue to DataProperty.sealed(NumberType.EPSILON.languageValue),
        "MAX_SAFE_INTEGER".languageValue to DataProperty.sealed(NumberType.MAX_SAFE_INTEGER.languageValue),
        "MAX_VALUE".languageValue to DataProperty.sealed(NumberType.MAX_VALUE.languageValue),
        "MIN_SAFE_INTEGER".languageValue to DataProperty.sealed(NumberType.MIN_SAFE_INTEGER.languageValue),
        "MIN_VALUE".languageValue to DataProperty.sealed(NumberType.MIN_VALUE.languageValue),
        "NaN".languageValue to DataProperty.sealed(NumberType.NaN),
        "NEGATIVE_INFINITY".languageValue to DataProperty.sealed(NumberType.NEGATIVE_INFINITY),
        "POSITIVE_INFINITY".languageValue to DataProperty.sealed(NumberType.POSITIVE_INFINITY),
        "from".languageValue to DataProperty.sealed(from),
        "isFinite".languageValue to DataProperty.sealed(isFinite),
        "isInteger".languageValue to DataProperty.sealed(isInteger),
        "isNaN".languageValue to DataProperty.sealed(isNaN),
        "isSafeInteger".languageValue to DataProperty.sealed(isSafeInteger),
        "parseLeadingDecimal".languageValue to DataProperty.sealed(parseLeadingDecimal),
        "parseLeadingInteger".languageValue to DataProperty.sealed(parseLeadingInteger),
        // TODO
    ),
    mutableMapOf(
        SymbolType.WellKnown.toString to DataProperty.sealed(toString),
        "toRadix".languageValue to DataProperty.sealed(toRadix),
        // TODO
    ),
    constructor {
        Completion.Throw(NullType/* TypeError */)
    },
)
