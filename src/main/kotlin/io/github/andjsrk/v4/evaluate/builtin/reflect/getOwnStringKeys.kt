package io.github.andjsrk.v4.evaluate.builtin.reflect

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("Object.getOwnPropertyNames")
val getOwnStringKeys = BuiltinFunctionType("getOwnStringKeys", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    Completion.Normal(
        ArrayType.from(
            obj._ownPropertyKeys().filterIsInstance<StringType>()
        )
    )
}
