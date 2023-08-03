package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.AccessorProperty
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

private val functionNameGetter = AccessorProperty.builtinGetter("name") fn@ {
    val func = it.requireToBe<FunctionType> { return@fn it }
    Completion.Normal(
        func.name ?: NullType
    )
}

val Function = BuiltinClassType(
    "Function",
    Object,
    mutableMapOf(
        // TODO
    ),
    mutableMapOf(
        "name".accessor(getter=functionNameGetter),
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "Function")
    },
)
