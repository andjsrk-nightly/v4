package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*

private val objectAssign = functionWithoutThis("assign", 2u) fn@ { args ->
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
                    .orReturnThrow { return@fn it }
                target.set(key, value)
                    .orReturnThrow { return@fn it }
            }
        }
    }
    target.toNormal()
}

private val objectEntries = functionWithoutThis("entries", 1u) fn@ { args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val entries = obj.ownEnumerableStringKeyEntries()
        .orReturnThrow { return@fn it }
    // TODO: migrate to generator
    ImmutableArrayType.from(entries.list)
        .toNormal()
}

@EsSpec("Object.freeze")
private val freeze = functionWithoutThis("freeze", 1u) fn@ { args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    obj.setImmutabilityLevel(ObjectImmutabilityLevel.FROZEN)
        .orReturnThrow { return@fn it }
    obj.toNormal()
}

@EsSpec("Object.fromEntries")
private val fromEntries = functionWithoutThis("fromEntries", 1u) { args ->
    val obj = ObjectType.createNormal()
    TODO()
}

// TODO: rename the function
@EsSpec("Object.keys")
private val getOwnStringKeys = functionWithoutThis("getOwnStringKeys", 1u) fn@ { args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    // TODO: migrate to generator
    ImmutableArrayType.from(
        obj.ownEnumerableStringPropertyKeys()
    )
        .toNormal()
}

// TODO: rename the function
@EsSpec("Object.values")
private val getOwnStringKeyValues = functionWithoutThis("getOwnStringKeyValues", 1u) fn@ { args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val values = obj.ownEnumerableStringPropertyKeyValues()
        .orReturnThrow { return@fn it }
    // TODO: migrate to generator
    ImmutableArrayType.from(values)
        .toNormal()
}

@EsSpec("Object.is")
private val objectIs = functionWithoutThis("is", 2u) { args ->
    sameValue(args[0], args[1])
        .languageValue
        .toNormal()
}

private val run = method("run", 1u) fn@ { thisArg, args ->
    val func = args[0].requireToBe<FunctionType> { return@fn it }
    func.call(thisArg, listOf(thisArg))
}

@EsSpec("%Object%")
val Object: BuiltinClassType by lazy {
    BuiltinClassType(
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
        { null },
        constructor ctor@ { obj, args ->
            val rawPrototype = args.getOptional(0)
            if (rawPrototype != null) {
                obj.prototype = rawPrototype
                    .normalizeNull()
                    ?.requireToBe<ObjectType> { return@ctor it }
            }
            empty
        },
    )
}
