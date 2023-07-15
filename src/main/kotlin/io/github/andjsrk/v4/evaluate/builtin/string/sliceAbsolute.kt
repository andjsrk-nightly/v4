package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val sliceAbsolute = builtinMethod("sliceAbsolute", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val start = args[0]
        .normalizeNull()
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeIndex { return@fn it }
        ?.coerceInString(string)
        ?: 0
    val end = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeIndex { return@fn it }
        ?.coerceInString(string)
        ?: string.length
    if (start > end) return@fn throwError(RangeErrorKind.SLICE_START_GREATER_THAN_END)
    Completion.Normal(
        string.substring(start, end)
            .languageValue
    )
}
