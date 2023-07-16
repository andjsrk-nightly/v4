package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.requireToBeString
import io.github.andjsrk.v4.evaluate.type.lang.SymbolType
import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod

val iterator = builtinMethod(SymbolType.WellKnown.iterator) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    TODO()
}
