package io.github.andjsrk.v4.evaluate.builtin.symbol.static

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val `for` = BuiltinFunctionType("for",  1u) { _, args ->
    val key = args[0]
    if (key !is StringType) return@BuiltinFunctionType Completion.Throw(NullType/* TypeError */)
    Completion.Normal(SymbolType.registry.getOrPut(key.value) { SymbolType(key) })
}
