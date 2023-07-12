package io.github.andjsrk.v4.evaluate.builtin.reflect

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

@EsSpec("Reflect.isExtensible")
val isExtensible = BuiltinFunctionType("isExtensible", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    Completion.Normal(
        obj.extensible.languageValue
    )
}
