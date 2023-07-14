package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*

val findMatchedIndex = builtinMethod("findMatchedIndex", 1u) fn@ { thisArg, args ->
    val stringArg = thisArg.requireToBe<StringType> { return@fn it }
    val generalArg = args[0] // intentionally does not coerce to regular expressions
    val findMatchedIndexMethod = generalArg.getMethod(SymbolType.WellKnown.findMatchedIndex)
        ?.returnIfAbrupt { return@fn it }
        ?: return@fn unexpectedType(generalArg, "a value that has Symbol.findMatchedIndex method")
    findMatchedIndexMethod._call(generalArg, listOf(stringArg))
}
