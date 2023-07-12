package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.getOptional
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod

val lastIndexOf = builtinMethod("lastIndexOf", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val search = args[0].requireToBeString { return@fn it }
    val stringEnd = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBePositionWithinString(string) { return@fn it }
        ?: 0
    Completion.Normal(
        string.lastIndexOf(search, stringEnd).languageValue
    )
}
