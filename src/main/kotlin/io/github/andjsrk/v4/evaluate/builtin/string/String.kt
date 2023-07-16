package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.builtin.sealedData
import io.github.andjsrk.v4.evaluate.builtin.string.static.*
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.evaluate.type.lang.SymbolType

@EsSpec("%String%")
val String = BuiltinClassType(
    "String",
    Object,
    mutableMapOf(
        sealedData(::from),
        sealedData(::fromCodePoint),
        sealedData(::fromCodeUnit),
        // TODO
    ),
    mutableMapOf(
        sealedData(::at),
        sealedData(::codePoint),
        sealedData(::codeUnit),
        sealedData(::concat),
        sealedData(::endsWith),
        sealedData(::findMatchedIndex),
        sealedData(::includes),
        sealedData(::indexOf),
        sealedData(::isWellFormed),
        sealedData(::lastIndexOf),
        sealedData(::localeCompare),
        sealedData(::match),
        sealedData(::matchAll),
        sealedData(::normalize),
        sealedData(::padEnd),
        sealedData(::padStart),
        sealedData(::repeat),
        sealedData(::replaceAll),
        sealedData(::replaceFirst),
        sealedData(::slice),
        sealedData(::sliceAbsolute),
        sealedData(::split),
        sealedData(::startsWith),
        sealedData(::toLocaleLowerCase),
        sealedData(::toLocaleUpperCase),
        sealedData(::toLowerCase),
        SymbolType.WellKnown.toString.sealedData(toString),
        sealedData(::toUpperCase),
        sealedData(::toUpperCase),
        sealedData(::toWellFormed),
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "String")
    },
)
