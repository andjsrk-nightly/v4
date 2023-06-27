package io.github.andjsrk.v4.evaluate.builtin.symbol.static

import io.github.andjsrk.v4.evaluate.normalizeNull
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.thenTake

val create = BuiltinFunctionType("create") fn@ { _, args ->
    val description = args.isNotEmpty().thenTake {
        args[0].normalizeNull()
    }
    if (description !is StringType?) return@fn Completion.Throw(NullType/* TypeError */)
    Completion.Normal(SymbolType(description))
}
