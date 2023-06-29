package io.github.andjsrk.v4.evaluate.builtin.`object`.static

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

@EsSpec("Object.freeze")
val freeze = BuiltinFunctionType("freeze", 1u) fn@ { _, args ->
    val obj = args[0]
        .requireToBe<ObjectType> { return@fn it }
    returnIfAbrupt(obj.setImmutabilityLevel(ObjectImmutabilityLevel.FROZEN)) { return@fn it }
    Completion.Normal(obj)
}
