package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val toString = builtinMethod(SymbolType.WellKnown.toString) fn@ { thisArg, _ ->
    thisArg.requireToBe<StringType> { return@fn it }
    Completion.Normal(thisArg)
}
