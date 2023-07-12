package io.github.andjsrk.v4.evaluate.builtin.`object`.static

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.returnIfAbrupt
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

// TODO: rename the function
@EsSpec("Object.values")
val getOwnEnumerableStringKeyValues = BuiltinFunctionType("getOwnEnumerableStringKeyValues", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val values = obj.ownEnumerableStringPropertyKeyValues()
        .returnIfAbrupt { return@fn it }
    Completion.Normal(
        ArrayType.from(values)
    )
}
