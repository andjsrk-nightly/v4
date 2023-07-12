package io.github.andjsrk.v4.evaluate.builtin.reflect

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey

@EsSpec("Reflect.getOwnPropertyDescriptor")
val getOwnPropertyDescriptor = BuiltinFunctionType("getOwnPropertyDescriptor", 2u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val key = args[1].requireToBe<PropertyKey> { return@fn it }
    val desc = obj._getOwnProperty(key)
    Completion.Normal(
        desc?.toDescriptorObject() ?: NullType
    )
}
