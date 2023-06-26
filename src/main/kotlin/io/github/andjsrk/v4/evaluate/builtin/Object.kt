package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

val Object = BuiltinClassType(
    null,
    mutableMapOf(
        "is".languageValue to DataProperty.sealed(
            BuiltinFunctionType("is".languageValue, 2u) { _, args ->
                Completion.Normal(sameValue(args[0], args[1]))
            }
        ),
        // TODO
    ),
    mutableMapOf(
        "run".languageValue to DataProperty.sealed(
            BuiltinFunctionType("run".languageValue, 1u) { thisArg, args ->
                val func = args[0]
                func.requireToBe<FunctionType> { return@BuiltinFunctionType it }
                func._call(thisArg, listOf(thisArg))
            }
        )
    ),
    constructor { _ ->
        Completion.Normal(ObjectType.createNormal())
    }
)
