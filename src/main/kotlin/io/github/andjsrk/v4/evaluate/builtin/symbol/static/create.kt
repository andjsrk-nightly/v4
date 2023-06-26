package io.github.andjsrk.v4.evaluate.builtin.symbol.static

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.thenTake

val create = BuiltinFunctionType("create") { _, args ->
    val description = args.isNotEmpty().thenTake {
        args[0].takeIf { it != NullType }
    }
    if (description !is StringType?) return@BuiltinFunctionType Completion.Throw(NullType/* TypeError */)
    Completion.Normal(SymbolType(description))
}
