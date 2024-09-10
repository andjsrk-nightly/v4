package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*

@EsSpec("Symbol([description])")
private val symbolCreate = functionWithoutThis("create") fn@ { args ->
    val description = args.getOptional(0)
        ?.requireToBeString { return@fn it }
    SymbolType(description)
        .toNormal()
}

@EsSpec("Symbol.for")
private val `for` = functionWithoutThis("for",  1u) fn@ { args ->
    val key = args[0].requireToBeString { return@fn it }
    val symbol = SymbolType.registry.getOrPut(key) { SymbolType(key) }
    symbol.toNormal()
}

@EsSpec("get Symbol.prototype.description")
private val descriptionGetter = getter("description") fn@ { thisArg ->
    val symbol = thisArg.requireToBe<SymbolType> { return@fn it }
    symbol.description?.languageValue.normalizeToNormal()
}

@EsSpec("%Symbol%")
val Symbol = BuiltinClassType(
    "Symbol",
    Object,
    mutableMapOf(
        sealedData(SymbolType.WellKnown::iterator),
        sealedData(SymbolType.WellKnown::toString),
        sealedMethod(symbolCreate),
        sealedMethod(`for`),
        // TODO
    ),
    mutableMapOf(
        "description".accessor(getter = descriptionGetter),
    ),
    { ObjectType.Impl() /* dummy object */ },
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "Symbol")
    },
)
