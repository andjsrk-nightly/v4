package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

private val functionNameGetter = getter("name") fn@ { thisArg ->
    val func = thisArg.requireToBe<FunctionType> { return@fn it }
    func.name?.toLanguageValue().normalizeToNormal()
}
private val functionNameSetter = setter("name") fn@ { thisArg, value ->
    val func = thisArg.requireToBe<FunctionType> { return@fn it }
    val name = value
        .normalizeNull()
        ?.requireToBeLanguageTypePropertyKey { return@fn it }
    func.name = name
    empty
}

val Function = BuiltinClassType(
    "Function",
    Object,
    mutableMapOf(),
    mutableMapOf(
        "name".accessor(getter=functionNameGetter, setter= functionNameSetter),
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "Function")
    },
)
