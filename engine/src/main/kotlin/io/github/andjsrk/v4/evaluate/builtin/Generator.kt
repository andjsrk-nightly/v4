package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

val generatorNext = method("next") fn@ { thisArg, args ->
    val gen = thisArg.requireToBe<SyncGeneratorType> { return@fn it }
    val input = args.getOptional(0) ?: NullType
    gen.resume(input)
}

val generatorClose = functionWithoutThis("close") {
    normalNull
}

val generatorIterator = method(SymbolType.WellKnown.iterator) { thisArg, _ ->
    thisArg.toNormal()
}

@EsSpec("%GeneratorFunction.prototype%")
val Generator = BuiltinClassType(
    "Generator",
    Object,
    mutableMapOf(),
    mutableMapOf(
        sealedMethod(generatorNext),
        sealedMethod(generatorClose),
        sealedMethod(generatorIterator),
    ),
    constructor ctor@ { _, args ->
        val sourceObj = args[0]
            .requireToBe<ObjectType> { return@ctor it }
        val nextMethod = sourceObj.getMethod("next".languageValue)
            .orReturn { return@ctor it }
        val closeMethod = sourceObj.getMethod("close".languageValue)
            .orReturn { return@ctor it }
        val gen = SyncGeneratorType()
        gen.definePropertyOrThrow("next".languageValue, DataProperty(nextMethod)).unwrap()
        if (closeMethod != null) gen.definePropertyOrThrow("close".languageValue, DataProperty(closeMethod)).unwrap()
        gen.toNormal()
    },
)
