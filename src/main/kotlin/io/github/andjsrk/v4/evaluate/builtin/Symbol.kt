package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.AccessorProperty
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

@EsSpec("Symbol([description])")
private val createSymbol = BuiltinFunctionType("create") fn@ { _, args ->
    val description = args.getOptional(0)
        ?.requireToBeString { return@fn it }
    Completion.Normal(
        SymbolType(description)
    )
}

@EsSpec("Symbol.for")
private val `for` = BuiltinFunctionType("for",  1u) fn@ { _, args ->
    val key = args[0].requireToBeString { return@fn it }
    val symbol = SymbolType.registry.getOrPut(key) { SymbolType(key) }
    Completion.Normal(symbol)
}

@EsSpec("get Symbol.prototype.description")
private val descriptionGetter = AccessorProperty.builtinGetter("description") fn@ { thisArg ->
    val symbol = thisArg.requireToBe<SymbolType> { return@fn it }
    Completion.Normal(
        symbol.description?.languageValue ?: NullType
    )
}

@EsSpec("%Symbol%")
val Symbol = BuiltinClassType(
    "Symbol",
    Object,
    mutableMapOf(
        sealedData(SymbolType.WellKnown::iterator),
        sealedData(SymbolType.WellKnown::toString),
        "create".sealedData(createSymbol),
        sealedData(::`for`),
        // TODO
    ),
    mutableMapOf(
        "description".accessor(getter=descriptionGetter),
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "Symbol")
    },
)
