package io.github.andjsrk.v4.evaluate.builtin.number

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.evaluate.type.lang.SymbolType
import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod

@EsSpec("Number.prototype.toString") // radix is fixed to 10
val toString = builtinMethod(SymbolType.WellKnown.toString) fn@ { thisArg, _ ->
    val number = thisArg.requireToBe<NumberType> { return@fn it }
    Completion.Normal(
        number.toString(10)
    )
}
