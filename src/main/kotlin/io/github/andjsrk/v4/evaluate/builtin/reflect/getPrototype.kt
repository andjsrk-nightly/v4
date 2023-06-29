package io.github.andjsrk.v4.evaluate.builtin.reflect

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.prototype
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.NullType

@EsSpec("Object.getPrototypeOf")
val getPrototype = BuiltinFunctionType("getPrototype", 1u) fn@ { _, args ->
    val value = args[0]
    Completion.Normal(
        value.prototype ?: NullType
    )
}
