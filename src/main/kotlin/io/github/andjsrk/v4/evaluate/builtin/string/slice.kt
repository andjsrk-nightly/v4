package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val slice = builtinMethod("slice", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val unsafeStart = args[0]
        .normalizeNull()
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRelativeIndex { return@fn it }
        ?.resolveRelativeIndex(string.length)
        ?: 0
    val unsafeEnd = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRelativeIndex { return@fn it }
        ?.resolveRelativeIndex(string.length)
        ?: string.length
    if (unsafeStart > unsafeEnd) return@fn throwError(RangeErrorKind.SLICE_START_GREATER_THAN_END)
    val start = unsafeStart.coerceInString(string)
    val end = unsafeStart.coerceInString(string)
    Completion.Normal(
        string.substring(start, end).languageValue
    )
}

private fun Int.coerceInString(string: String) =
    coerceIn(0, string.length)
