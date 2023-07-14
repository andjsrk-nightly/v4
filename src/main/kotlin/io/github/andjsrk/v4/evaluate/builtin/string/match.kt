package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*

val match = builtinMethod("match", 1u) fn@ { thisArg, args ->
    val stringArg = thisArg.requireToBe<StringType> { return@fn it }
    val generalArg = args[0]
    val matchMethod = generalArg.getMethod(SymbolType.WellKnown.match)
        ?.returnIfAbrupt { return@fn it }
        ?: return@fn unexpectedType(generalArg, "a value that has Symbol.match method")
    matchMethod._call(generalArg, listOf(stringArg, BooleanType.FALSE))
}
