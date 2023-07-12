package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.builtin.`object`.Object
import io.github.andjsrk.v4.evaluate.builtin.sealedData
import io.github.andjsrk.v4.evaluate.builtin.string.static.*
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

@EsSpec("%String%")
val String = BuiltinClassType(
    "String",
    Object,
    mutableMapOf(
        "from".sealedData(from),
        "fromCodePoint".sealedData(fromCodePoint),
        "fromCodeUnit".sealedData(fromCodeUnit),
        // TODO
    ),
    mutableMapOf(
        "at".sealedData(at),
        "codePoint".sealedData(codePoint),
        "codeUnit".sealedData(codeUnit),
        "concat".sealedData(concat),
        "endsWith".sealedData(endsWith),
        "includes".sealedData(includes),
        "indexOf".sealedData(indexOf),
        "isWellFormed".sealedData(isWellFormed),
        "lastIndexOf".sealedData(lastIndexOf),
        "localeCompare".sealedData(localeCompare),
        "match".sealedData(match),
        "matchAll".sealedData(matchAll),
        "normalize".sealedData(normalize),
        "padEnd".sealedData(padEnd),
        "padStart".sealedData(padStart),
        "repeat".sealedData(repeat),
        "replaceFirst".sealedData(replaceFirst),
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "String")
    },
)
