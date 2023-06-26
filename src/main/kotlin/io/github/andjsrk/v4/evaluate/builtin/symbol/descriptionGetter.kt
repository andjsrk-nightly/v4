package io.github.andjsrk.v4.evaluate.builtin.symbol

import io.github.andjsrk.v4.evaluate.type.AccessorProperty
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.lang.SymbolType

val descriptionGetter = AccessorProperty.builtinGetter("description") { thisArg ->
    if (thisArg !is SymbolType) return@builtinGetter Completion.Throw(NullType/* TypeError */)
    Completion.Normal(thisArg.description ?: NullType)
}
