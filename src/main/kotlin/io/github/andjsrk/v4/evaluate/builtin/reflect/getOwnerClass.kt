package io.github.andjsrk.v4.evaluate.builtin.reflect

import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.PrototypeObjectType

val getOwnerClass = BuiltinFunctionType("getOwnerClass", 1u) fn@ { _, args ->
    val proto = args[0].requireToBe<PrototypeObjectType> { return@fn it }
    Completion.Normal(proto.ownerClass)
}
