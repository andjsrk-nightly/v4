package io.github.andjsrk.v4.evaluate.builtin.boolean

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType
import io.github.andjsrk.v4.evaluate.type.lang.SymbolType
import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod

val toString = builtinMethod(SymbolType.WellKnown.toString) fn@ { thisArg, _ ->
    val boolean = thisArg.requireToBe<BooleanType> { return@fn it }
    Completion.Normal(
        boolean.value.toString().languageValue
    )
}
