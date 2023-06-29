package io.github.andjsrk.v4.evaluate.builtin.number

import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val toString = BuiltinFunctionType(SymbolType.WellKnown.toString) fn@ { thisArg, _ ->
    val number = thisArg.requireToBe<NumberType> { return@fn it }
    Completion.Normal(
        number.toString(10)
    )
}
