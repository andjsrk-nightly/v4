package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

@EsSpec("Object.assign")
private val assignEnumerableProperties = BuiltinFunctionType("assignEnumerableProperties", 2u) fn@ { _, args ->
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

@EsSpec("Object.create")
private val createObject = BuiltinFunctionType("create", 1u) fn@ { _, args ->
    val prototype = args[0]
        .normalizeNull()
        ?.requireToBe<PrototypeObjectType> { return@fn it }
    Completion.Normal(
        ObjectType.create(prototype)
    )
}

@EsSpec("Object.entries")
private val entries = BuiltinFunctionType("entries", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val entries = obj.ownEnumerableStringPropertyKeyEntries()
        .returnIfAbrupt { return@fn it }
    Completion.Normal(
        ArrayType.from(entries)
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
private val getOwnEnumerableStringKeys = BuiltinFunctionType("getOwnEnumerableStringKeys", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    Completion.Normal(
        ArrayType.from(
            obj.ownEnumerableStringPropertyKeys()
        )
    )
}

// TODO: rename the function
@EsSpec("Object.values")
private val getOwnEnumerableStringKeyValues = BuiltinFunctionType("getOwnEnumerableStringKeyValues", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val values = obj.ownEnumerableStringPropertyKeyValues()
        .returnIfAbrupt { return@fn it }
    Completion.Normal(
        ArrayType.from(values)
    )
}

@EsSpec("Object.is")
private val objectIs = BuiltinFunctionType("is", 2u) { _, args ->
    Completion.Normal(sameValue(args[0], args[1]))
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
        sealedData(::assignEnumerableProperties),
        "create".sealedData(createObject),
        sealedData(::entries),
        sealedData(::fromEntries),
        sealedData(::freeze),
        sealedData(::getOwnEnumerableStringKeys),
        sealedData(::getOwnEnumerableStringKeyValues),
        "is".sealedData(objectIs),
        // TODO
    ),
    mutableMapOf(
        sealedData(::run),
    ),
    constructor { obj, _ ->
        Completion.Normal(obj)
    },
)
