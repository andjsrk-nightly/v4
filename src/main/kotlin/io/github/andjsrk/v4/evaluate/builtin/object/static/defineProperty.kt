package io.github.andjsrk.v4.evaluate.builtin.`object`.static

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val defineProperty = BuiltinFunctionType("defineProperty", 3u) fn@ { _, args ->
    val obj = args[0]
        .requireToBe<ObjectType> { return@fn it }
    val key = args[1]
        .requireToBe<PropertyKey> { return@fn it }
    val descObj = args[2]
        .requireToBe<ObjectType> { return@fn it }
    val desc = returnIfAbrupt(descObj.toPropertyDescriptor()) { return@fn it }
    returnIfAbrupt(obj.definePropertyOrThrow(key, desc)) { return@fn it }
    Completion.Normal(obj)
}
