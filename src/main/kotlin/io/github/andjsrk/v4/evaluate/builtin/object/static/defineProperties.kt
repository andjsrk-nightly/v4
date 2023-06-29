package io.github.andjsrk.v4.evaluate.builtin.`object`.static

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.Property
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.not

val defineProperties = BuiltinFunctionType("defineProperties", 2u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    // descriptors should not be a Map, because each descriptor can cause an error when define the property
    val descriptors = mutableListOf<Pair<PropertyKey, Property>>()
    val props = args[1].requireToBe<ObjectType> { return@fn it }
    for ((key, desc) in props.ownPropertyEntries()) {
        if (desc.not { enumerable }) continue
        val propDescObj = returnIfAbrupt(props.get(key)) { return@fn it }
            .requireToBe<ObjectType> { return@fn it }
        val propDesc = returnIfAbrupt(propDescObj.toPropertyDescriptor()) { return@fn it }
        descriptors += key to propDesc
    }
    for ((key, desc) in descriptors) returnIfAbrupt(obj.definePropertyOrThrow(key, desc)) { return@fn it }
    Completion.Normal(obj)
}
