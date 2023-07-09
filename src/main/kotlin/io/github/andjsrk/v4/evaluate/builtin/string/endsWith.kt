package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.getOptional
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType

val endsWith = BuiltinFunctionType("endsWith", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val search = args[0].requireToBeString { return@fn it }
    val endPosition = args.getOptional(1)
        ?.requireToBeStartIndex(string, "endPosition") { return@fn it }
        ?: string.length
    Completion.Normal(
        string.dropLast(string.length - endPosition)
            .endsWith(search)
            .languageValue
    )
}
