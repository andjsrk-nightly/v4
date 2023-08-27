package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.throwError
import io.github.andjsrk.v4.evaluate.type.getter
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType
import io.github.andjsrk.v4.evaluate.type.lang.FunctionType
import io.github.andjsrk.v4.evaluate.type.lang.constructor
import io.github.andjsrk.v4.evaluate.type.normalizeToNormal

private val functionNameGetter = getter("name") fn@ {
    val func = it.requireToBe<FunctionType> { return@fn it }
    func.name.normalizeToNormal()
}

val Function = BuiltinClassType(
    "Function",
    Object,
    mutableMapOf(),
    mutableMapOf(
        "name".accessor(getter=functionNameGetter),
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "Function")
    },
)
