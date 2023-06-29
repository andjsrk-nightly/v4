package io.github.andjsrk.v4.evaluate.builtin.symbol.static

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("Symbol.for")
val `for` = BuiltinFunctionType("for",  1u) fn@ { _, args ->
    val key = args[0]
        .requireToBe<StringType> { return@fn it }
    val symbol = SymbolType.registry.getOrPut(key.value) { SymbolType(key) }
    Completion.Normal(symbol)
}
