package io.github.andjsrk.v4.evaluate.builtin.`object`.static

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

@EsSpec("Object.assign")
val assignEnumerableProperties = BuiltinFunctionType("assignEnumerableProperties", 2u) fn@ { _, args ->
    val target = args[0]
        .requireToBe<ObjectType> { return@fn it }
    val sources = args
        .drop(1)
        .map {
            it.normalizeNull()
                .requireToBe<ObjectType?> { return@fn it }
        }
    for (source in sources) {
        if (source == null) continue
        for ((key, desc) in source.ownPropertyEntries()) {
            if (desc.enumerable) {
                val value = returnIfAbrupt(source.get(key)) { return@fn it }
                returnIfAbrupt(target.set(key, value)) { return@fn it }
            }
        }
    }
    Completion.Normal(target)
}
