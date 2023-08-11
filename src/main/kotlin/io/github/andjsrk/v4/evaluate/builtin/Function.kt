package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import io.github.andjsrk.v4.evaluate.type.lang.FunctionType

private val functionNameGetter = AccessorProperty.builtinGetter("name") fn@ {
    val func = it.requireToBe<FunctionType> { return@fn it }
    func.name?.toNormal() ?: `null`
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
