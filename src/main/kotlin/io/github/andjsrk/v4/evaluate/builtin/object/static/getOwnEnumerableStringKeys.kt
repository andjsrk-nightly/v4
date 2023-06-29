package io.github.andjsrk.v4.evaluate.builtin.`object`.static

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

// TODO: rename the function
@EsSpec("Object.keys")
val getOwnEnumerableStringKeys = BuiltinFunctionType("getOwnEnumerableStringKeys", 1u) fn@ { _, args ->
    val obj = args[0]
        .requireToBe<ObjectType> { return@fn it }
    Completion.Normal(
        ArrayType.from(
            obj.ownEnumerableStringPropertyKeys()
        )
    )
}
