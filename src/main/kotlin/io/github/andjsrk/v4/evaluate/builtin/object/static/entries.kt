package io.github.andjsrk.v4.evaluate.builtin.`object`.static

import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.returnIfAbrupt
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val entries = BuiltinFunctionType("entries", 1u) fn@ { _, args ->
    val obj = args[0]
        .requireToBe<ObjectType> { return@fn it }
    val entries = returnIfAbrupt(obj.ownEnumerableStringPropertyKeyEntries()) { return@fn it }
    Completion.Normal(
        ArrayType.from(entries)
    )
}
