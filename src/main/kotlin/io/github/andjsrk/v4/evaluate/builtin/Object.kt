package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

private val objectAssign = BuiltinFunctionType("assign", 2u) fn@ { _, args ->
    val target = args[0].requireToBe<ObjectType> { return@fn it }
    val sources = args
        .drop(1)
        .map {
            it.normalizeNull()
                ?.requireToBe<ObjectType> { return@fn it }
        }
    for (source in sources) {
        if (source == null) continue
        for ((key, desc) in source.ownPropertyEntries()) {
            if (desc.enumerable) {
                val value = source.get(key)
                    .returnIfAbrupt { return@fn it }
                target.set(key, value)
                    .returnIfAbrupt { return@fn it }
            }
        }
    }
    Completion.Normal(target)
}

private val objectEntries = BuiltinFunctionType("entries", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val entries = obj.ownEnumerableStringKeyEntries()
        .returnIfAbrupt { return@fn it }
    // TODO: migrate to generator
    Completion.Normal(
        ImmutableArrayType.from(entries.list)
    )
}

@EsSpec("Object.freeze")
private val freeze = BuiltinFunctionType("freeze", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    obj.setImmutabilityLevel(ObjectImmutabilityLevel.FROZEN)
        .returnIfAbrupt { return@fn it }
    Completion.Normal(obj)
}

@EsSpec("Object.fromEntries")
private val fromEntries = BuiltinFunctionType("fromEntries", 1u) { _, args ->
    val obj = ObjectType.createNormal()
    TODO()
}

// TODO: rename the function
@EsSpec("Object.keys")
private val getOwnStringKeys = BuiltinFunctionType("getOwnStringKeys", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    // TODO: migrate to generator
    Completion.Normal(
        ImmutableArrayType.from(
            obj.ownEnumerableStringPropertyKeys()
        )
    )
}

// TODO: rename the function
@EsSpec("Object.values")
private val getOwnStringKeyValues = BuiltinFunctionType("getOwnStringKeyValues", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val values = obj.ownEnumerableStringPropertyKeyValues()
        .returnIfAbrupt { return@fn it }
    // TODO: migrate to generator
    Completion.Normal(
        ImmutableArrayType.from(values)
    )
}

@EsSpec("Object.is")
private val objectIs = BuiltinFunctionType("is", 2u) { _, args ->
    Completion.Normal(
        sameValue(args[0], args[1])
            .languageValue
    )
}

private val run = builtinMethod("run", 1u) fn@ { thisArg, args ->
    val func = args[0].requireToBe<FunctionType> { return@fn it }
    func._call(thisArg, listOf(thisArg))
}

@EsSpec("%Object%")
val Object = BuiltinClassType(
    "Object",
    null,
    mutableMapOf(
        sealedMethod(objectAssign),
        sealedMethod(objectEntries),
        sealedMethod(fromEntries),
        sealedMethod(freeze),
        sealedMethod(getOwnStringKeys),
        sealedMethod(getOwnStringKeyValues),
        sealedMethod(objectIs),
        // TODO
    ),
    mutableMapOf(
        sealedMethod(run),
    ),
    constructor ctor@ { _, args ->
        val prototype = args.getOptional(0)
            ?.normalizeNull()
            ?.requireToBe<PrototypeObjectType> { return@ctor it }
        val obj = ObjectType.create(prototype)
        Completion.Normal(
            ObjectType.create(prototype)
        )
    },
)
