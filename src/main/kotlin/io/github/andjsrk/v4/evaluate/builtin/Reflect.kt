package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.Property
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.not

@EsSpec("Object.defineProperties")
private val defineProperties = BuiltinFunctionType("defineProperties", 2u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val props = args[1].requireToBe<ObjectType> { return@fn it }
    // descriptors should not be a Map, because each descriptor can cause an error when define the property
    val descriptors = mutableListOf<Pair<PropertyKey, Property>>()
    for ((key, desc) in props.ownPropertyEntries()) {
        if (desc.not { enumerable }) continue
        val propDescObj = props.get(key)
            .returnIfAbrupt { return@fn it }
            .requireToBe<ObjectType> { return@fn it }
        val propDesc = propDescObj.toPropertyDescriptor()
            .returnIfAbrupt { return@fn it }
        descriptors += key to propDesc
    }
    for ((key, desc) in descriptors) {
        obj.definePropertyOrThrow(key, desc)
            .returnIfAbrupt { return@fn it }
    }
    Completion.Normal.`null`
}

@EsSpec("Reflect.defineProperty")
private val defineProperty = BuiltinFunctionType("defineProperty", 3u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val key = args[1].requireToBe<PropertyKey> { return@fn it }
    val descObj = args[2].requireToBe<ObjectType> { return@fn it }
    val desc = descObj.toPropertyDescriptor()
        .returnIfAbrupt { return@fn it }
    obj.definePropertyOrThrow(key, desc)
        .returnIfAbrupt { return@fn it }
    Completion.Normal.`null`
}

private val getOwnerClass = BuiltinFunctionType("getOwnerClass", 1u) fn@ { _, args ->
    val proto = args[0].requireToBe<PrototypeObjectType> { return@fn it }
    Completion.Normal(proto.ownerClass)
}

@EsSpec("Reflect.ownKeys")
private val getOwnKeys = BuiltinFunctionType("getOwnKeys", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    Completion.Normal(
        ImmutableArrayType.from(
            obj._ownPropertyKeys()
        )
    )
}

@EsSpec("Reflect.getOwnPropertyDescriptor")
private val getOwnPropertyDescriptor = BuiltinFunctionType("getOwnPropertyDescriptor", 2u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val key = args[1].requireToBe<PropertyKey> { return@fn it }
    val desc = obj._getOwnProperty(key)
    Completion.Normal(
        desc?.toDescriptorObject() ?: NullType
    )
}

@EsSpec("Object.getOwnPropertyDescriptors")
private val getOwnPropertyDescriptors = BuiltinFunctionType("getOwnPropertyDescriptors", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val res = ObjectType.createNormal()
    for ((key, desc) in obj.ownPropertyEntries()) res.createDataProperty(key, desc.toDescriptorObject())
    Completion.Normal(res)
}

@EsSpec("Object.getOwnPropertyNames")
private val getOwnStringKeys = BuiltinFunctionType("getOwnStringKeys", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    Completion.Normal(
        ImmutableArrayType.from(
            obj._ownPropertyKeys().filterIsInstance<StringType>()
        )
    )
}

@EsSpec("Object.getOwnPropertySymbols")
private val getOwnSymbolKeys = BuiltinFunctionType("getOwnSymbolKeys", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    Completion.Normal(
        ImmutableArrayType.from(
            obj._ownPropertyKeys().filterIsInstance<SymbolType>()
        )
    )
}

@EsSpec("Object.getPrototypeOf")
private val getPrototype = BuiltinFunctionType("getPrototype", 1u) fn@ { _, args ->
    val value = args[0]
    Completion.Normal(
        value.prototype ?: NullType
    )
}

@EsSpec("Object.prototype.propertyIsEnumerable")
private val isEnumerableProperty = BuiltinFunctionType("isEnumerableProperty", 2u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    val key = args[1].requireToBe<PropertyKey> { return@fn it }
    val desc = obj._getOwnProperty(key) ?: return@fn Completion.Normal(BooleanType.FALSE)
    Completion.Normal(BooleanType.from(desc.enumerable))
}

@EsSpec("Reflect.isExtensible")
private val isExtensible = BuiltinFunctionType("isExtensible", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    Completion.Normal(
        obj.extensible.languageValue
    )
}

@EsSpec("Reflect.preventExtensions")
private val preventExtensions = BuiltinFunctionType("preventExtensions", 1u) fn@ { _, args ->
    val obj = args[0].requireToBe<ObjectType> { return@fn it }
    obj.extensible = false
    Completion.Normal.`null`
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
