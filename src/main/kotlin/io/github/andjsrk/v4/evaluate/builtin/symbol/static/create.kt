package io.github.andjsrk.v4.evaluate.builtin.symbol.static

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.normalizeNull
import io.github.andjsrk.v4.evaluate.requireToBeNullable
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("Symbol([description])")
val create = BuiltinFunctionType("create") fn@ { _, args ->
    val description = args.getOrNull(0)
        .normalizeNull()
        .requireToBeNullable<StringType> { return@fn it }
    Completion.Normal(
        SymbolType(description)
    )
}
