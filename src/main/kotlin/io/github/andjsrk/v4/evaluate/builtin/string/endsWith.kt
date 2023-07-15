package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val endsWith = builtinMethod("endsWith", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val search = args[0].requireToBeString { return@fn it }
    val stringEnd = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeIndex { return@fn it }
        ?.coerceAtMost(string.length)
        ?: string.length
    Completion.Normal(
        string.dropLast(string.length - stringEnd)
            .endsWith(search)
            .languageValue
    )
}
