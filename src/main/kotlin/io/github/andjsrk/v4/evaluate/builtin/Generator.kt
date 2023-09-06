package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal

@EsSpec("%GeneratorFunction.prototype%")
val Generator = BuiltinClassType(
    "Generator",
    Object,
    mutableMapOf(),
    mutableMapOf(),
    constructor ctor@ { _, args ->
        val sourceObj = args[0]
            .requireToBe<ObjectType> { return@ctor it }
        val nextMethod = sourceObj.getMethod("next".languageValue)
            .orReturn { return@ctor it }
        val returnMethod = sourceObj.getMethod("return".languageValue)
            .orReturn { return@ctor it }
        val gen = SyncGeneratorType()
        gen.definePropertyOrThrow("next".languageValue, DataProperty(nextMethod)).unwrap()
        if (returnMethod != null) gen.definePropertyOrThrow("return".languageValue, DataProperty(returnMethod)).unwrap()
        gen.toNormal()
    },
)
