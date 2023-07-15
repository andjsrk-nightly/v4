package io.github.andjsrk.v4.evaluate.builtin.symbol

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.AccessorProperty
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.lang.SymbolType

@EsSpec("get Symbol.prototype.description")
val descriptionGetter = AccessorProperty.builtinGetter("description") fn@ { thisArg ->
    val symbol = thisArg.requireToBe<SymbolType> { return@fn it }
    Completion.Normal(
        symbol.description?.languageValue ?: NullType
    )
}
