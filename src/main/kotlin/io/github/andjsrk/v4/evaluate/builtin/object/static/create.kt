package io.github.andjsrk.v4.evaluate.builtin.`object`.static

import io.github.andjsrk.v4.evaluate.normalizeNull
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val create = BuiltinFunctionType("create", 1u) fn@ { _, args ->
    val prototype = args[0]
        .normalizeNull()
        .requireToBe<PrototypeObjectType?> { return@fn it }
    Completion.Normal(
        ObjectType.create(prototype)
    )
}
