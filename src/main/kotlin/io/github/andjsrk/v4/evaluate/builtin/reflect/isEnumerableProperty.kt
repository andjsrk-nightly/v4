package io.github.andjsrk.v4.evaluate.builtin.reflect

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("Object.prototype.propertyIsEnumerable")
val isEnumerableProperty = BuiltinFunctionType("isEnumerableProperty", 2u) fn@ { _, args ->
    val obj = args[0]
        .requireToBe<ObjectType> { return@fn it }
    val key = args[1]
        .requireToBe<PropertyKey> { return@fn it }
    val desc = obj._getOwnProperty(key) ?: return@fn Completion.Normal(BooleanType.FALSE)
    Completion.Normal(BooleanType.from(desc.enumerable))
}
