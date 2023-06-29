package io.github.andjsrk.v4.evaluate.builtin.`object`.static

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

@EsSpec("Object.fromEntries")
val fromEntries = BuiltinFunctionType("fromEntries", 1u) { _, args ->
    val obj = ObjectType.createNormal()
    ObjectType
    TODO()
}
