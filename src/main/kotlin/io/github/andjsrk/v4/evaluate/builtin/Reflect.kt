package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.not

@EsSpec("Object.defineProperties")
private val defineProperties = functionWithoutThis("defineProperties", 2u) fn@ { args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val props = args[1].requireToBe<ObjectType> { return@fn it }
    // descriptors should not be a Map, because each descriptor can cause an error when define the property
    val descriptors = mutableListOf<Pair<PropertyKey, Property>>()
    for ((key, desc) in props.ownPropertyEntries()) {
        if (desc.not { enumerable }) continue
        val propDescObj = props.get(key)
            .orReturn { return@fn it }
            .requireToBe<ObjectType> { return@fn it }
        val propDesc = propDescObj.toPropertyDescriptor()
            .orReturn { return@fn it }
        descriptors += key to propDesc
    }
    for ((key, desc) in descriptors) {
        obj.definePropertyOrThrow(key, desc)
            .orReturn { return@fn it }
    }
    normalNull
}

@EsSpec("Reflect.defineProperty")
private val defineProperty = functionWithoutThis("defineProperty", 3u) fn@ { args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val key = args[1].requireToBe<PropertyKey> { return@fn it }
    val descObj = args[2].requireToBe<ObjectType> { return@fn it }
    val desc = descObj.toPropertyDescriptor()
        .orReturn { return@fn it }
    obj.definePropertyOrThrow(key, desc)
        .orReturn { return@fn it }
    normalNull
}

private val getOwnerClass = functionWithoutThis("getOwnerClass", 1u) fn@ { args ->
    val proto = args[0].requireToBe<PrototypeObjectType> { return@fn it }
    proto.ownerClass.toNormal()
}

@EsSpec("Reflect.ownKeys")
private val getOwnKeys = functionWithoutThis("getOwnKeys", 1u) fn@ { args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    ImmutableArrayType.from(
        obj._ownPropertyKeys()
    )
        .toNormal()
}

@EsSpec("Reflect.getOwnPropertyDescriptor")
private val getOwnPropertyDescriptor = functionWithoutThis("getOwnPropertyDescriptor", 2u) fn@ { args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val key = args[1].requireToBe<PropertyKey> { return@fn it }
    val desc = obj._getOwnProperty(key)
    desc?.toDescriptorObject().normalizeToNormal()
}

@EsSpec("Object.getOwnPropertyDescriptors")
private val getOwnPropertyDescriptors = functionWithoutThis("getOwnPropertyDescriptors", 1u) fn@ { args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val res = ObjectType.createNormal()
    for ((key, desc) in obj.ownPropertyEntries()) res.createDataProperty(key, desc.toDescriptorObject())
    res.toNormal()
}

@EsSpec("Object.getOwnPropertyNames")
private val getOwnStringKeys = functionWithoutThis("getOwnStringKeys", 1u) fn@ { args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    ImmutableArrayType.from(
        obj._ownPropertyKeys().filterIsInstance<StringType>()
    )
        .toNormal()
}

@EsSpec("Object.getOwnPropertySymbols")
private val getOwnSymbolKeys = functionWithoutThis("getOwnSymbolKeys", 1u) fn@ { args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    ImmutableArrayType.from(
        obj._ownPropertyKeys().filterIsInstance<SymbolType>()
    )
        .toNormal()
}

@EsSpec("Object.getPrototypeOf")
private val getPrototype = functionWithoutThis("getPrototype", 1u) fn@ { args ->
    val value = args[0]
    value.prototype.normalizeToNormal()
}

@EsSpec("Object.prototype.propertyIsEnumerable")
private val isEnumerableProperty = functionWithoutThis("isEnumerableProperty", 2u) fn@{ args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val key = args[1].requireToBe<PropertyKey> { return@fn it }
    val desc = obj._getOwnProperty(key) ?: return@fn BooleanType.FALSE.toNormal()
    desc.enumerable
        .languageValue
        .toNormal()
}

@EsSpec("Reflect.isExtensible")
private val isExtensible = functionWithoutThis("isExtensible", 1u) fn@ { args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    obj.extensible
        .languageValue
        .toNormal()
}

@EsSpec("Reflect.preventExtensions")
private val preventExtensions = functionWithoutThis("preventExtensions", 1u) fn@ { args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    obj.extensible = false
    normalNull
}

@EsSpec("%Reflect%")
val Reflect = ObjectType(properties=mutableMapOf(
    sealedMethod(defineProperty),
    sealedMethod(defineProperties),
    sealedMethod(getOwnPropertyDescriptor),
    sealedMethod(getOwnPropertyDescriptors),
    sealedMethod(getOwnKeys),
    sealedMethod(getOwnStringKeys),
    sealedMethod(getOwnSymbolKeys),
    sealedMethod(getOwnerClass),
    sealedMethod(getPrototype),
    sealedMethod(isEnumerableProperty),
    sealedMethod(isExtensible),
    sealedMethod(preventExtensions),
))
