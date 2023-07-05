package io.github.andjsrk.v4.evaluate.builtin.error

import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType

internal fun createNativeErrorClass(name: String) =
    BuiltinClassType(
        name,
        Error,
        mutableMapOf(),
        mutableMapOf(),
        Error.constructor,
    )
