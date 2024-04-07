package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*

private val generatorNext = method("next") fn@ { thisArg, args ->
    val gen = thisArg
        .requireToBe<SyncGeneratorType> { return@fn it }
    val input = args.getOptional(0) ?: NullType
    gen.resume(input)
}

private val generatorClose = functionWithoutThis("close") {
    normalNull
}

@EsSpec("%GeneratorFunction.prototype%")
val Generator = BuiltinClassType(
    "Generator",
    IteratorInstancePrototype,
    mutableMapOf(),
    mutableMapOf(
        sealedMethod(generatorNext),
        sealedMethod(generatorClose),
    ),
    { SyncGeneratorType() },
    constructor ctor@ { gen, args ->
        val sourceObj = args[0]
            .requireToBe<ObjectType> { return@ctor it }
        val nextMethod = sourceObj.getMethod("next".languageValue)
            .orReturnThrow { return@ctor it }
        val closeMethod = sourceObj.getMethod("close".languageValue)
            .orReturnThrow { return@ctor it }
        gen.definePropertyOrThrow("next".languageValue, DataProperty(nextMethod)).unwrap()
        if (closeMethod != null) gen.definePropertyOrThrow("close".languageValue, DataProperty(closeMethod)).unwrap()
        empty
    },
)
