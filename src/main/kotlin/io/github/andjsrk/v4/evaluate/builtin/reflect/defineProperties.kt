package io.github.andjsrk.v4.evaluate.builtin.reflect

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.Property
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.not

@EsSpec("Object.defineProperties")
val defineProperties = BuiltinFunctionType("defineProperties", 2u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val props = args[1].requireToBe<ObjectType> { return@fn it }
    // descriptors should not be a Map, because each descriptor can cause an error when define the property
    val descriptors = mutableListOf<Pair<PropertyKey, Property>>()
    for ((key, desc) in props.ownPropertyEntries()) {
        if (desc.not { enumerable }) continue
        val propDescObj = props.get(key)
            .returnIfAbrupt { return@fn it }
            .requireToBe<ObjectType> { return@fn it }
        val propDesc = propDescObj.toPropertyDescriptor()
            .returnIfAbrupt { return@fn it }
        descriptors += key to propDesc
    }
    for ((key, desc) in descriptors) {
        obj.definePropertyOrThrow(key, desc)
            .returnIfAbrupt { return@fn it }
    }
    Completion.Normal.`null`
}
