package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod

val indexOf = builtinMethod("indexOf", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val search = args[0].requireToBeString { return@fn it }
    val startIndex = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeIndexWithinString(string) { return@fn it }
        ?: 0
    Completion.Normal(
        string.indexOf(search, startIndex).languageValue
    )
}
