package io.github.andjsrk.v4.evaluate.builtin.reflect

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.returnIfAbrupt
import io.github.andjsrk.v4.evaluate.toPropertyDescriptor
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey

@EsSpec("Reflect.defineProperty")
val defineProperty = BuiltinFunctionType("defineProperty", 3u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val key = args[1].requireToBe<PropertyKey> { return@fn it }
    val descObj = args[2].requireToBe<ObjectType> { return@fn it }
    val desc = descObj.toPropertyDescriptor()
        .returnIfAbrupt { return@fn it }
    obj.definePropertyOrThrow(key, desc)
        .returnIfAbrupt { return@fn it }
    Completion.Normal.`null`
}
