package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.thenTake

val Symbol = BuiltinClassType(
    Object,
    mutableMapOf(
        "iterator".languageValue to DataProperty.sealed(SymbolType.WellKnown.iterator),
        "toString".languageValue to DataProperty.sealed(SymbolType.WellKnown.toString),
        "create".languageValue to DataProperty.sealed(
            BuiltinFunctionType("create") { _, args ->
                val description = args.isNotEmpty().thenTake {
                    args[0].takeIf { it != NullType }
                }
                if (description != null && description !is StringType) return@BuiltinFunctionType Completion.Throw(NullType/* TypeError */)
                require(description is StringType?)
                Completion.Normal(SymbolType(description))
            }
        )
        // TODO
    ),
    mutableMapOf(
        "description".languageValue to AccessorProperty(
            AccessorProperty.builtinGetter("description") { thisArg ->
                require(thisArg is SymbolType)
                Completion.Normal(thisArg.description ?: NullType)
            },
        ),
        // TODO
    ),
    constructor {
        Completion.Throw(NullType/* TypeError */)
    },
)
