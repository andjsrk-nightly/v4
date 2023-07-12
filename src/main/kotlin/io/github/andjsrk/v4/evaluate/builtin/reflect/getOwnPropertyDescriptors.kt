package io.github.andjsrk.v4.evaluate.builtin.reflect

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

@EsSpec("Object.getOwnPropertyDescriptors")
val getOwnPropertyDescriptors = BuiltinFunctionType("getOwnPropertyDescriptors", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val res = ObjectType.createNormal()
    for ((key, desc) in obj.ownPropertyEntries()) res.createDataProperty(key, desc.toDescriptorObject())
    Completion.Normal(res)
}
