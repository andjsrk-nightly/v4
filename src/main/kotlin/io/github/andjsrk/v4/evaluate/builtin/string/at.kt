package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val at = builtinMethod("at", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val index = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeRelativeIndex(string.length) { return@fn it }
        .resolveRelativeIndex(string.length)
        ?: return@fn Completion.Normal.`null`
    Completion.Normal(
        string.getOrNull(index)?.toString()?.languageValue ?: NullType
    )
}
