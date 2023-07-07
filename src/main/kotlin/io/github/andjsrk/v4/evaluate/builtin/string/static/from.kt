package io.github.andjsrk.v4.evaluate.builtin.string.static

import io.github.andjsrk.v4.evaluate.stringify
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType

val from = BuiltinFunctionType("from", 1u) fn@ { _, args ->
    val value = args[0]
    stringify(value)
}
