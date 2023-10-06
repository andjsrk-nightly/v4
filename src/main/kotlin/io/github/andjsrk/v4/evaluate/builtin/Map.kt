package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("Map.prototype.clear")
private val mapClear = method("clear") fn@ { thisArg, _ ->
    val map = thisArg.requireToBe<MapType> { return@fn it }
    withUnsafeModification({ return@fn it }) {
        map.map.clear()
    }
    normalNull
}

@EsSpec("Map.prototype.delete")
private val mapRemove = method("remove", 1u) fn@ { thisArg, args ->
    val map = thisArg.requireToBe<MapType> { return@fn it }
    val key = args[0]
    var removed = false
    withUnsafeModification({ return@fn it }) {
        removed = map.map.entries.removeIf { (k) -> sameValue(k, key) }
    }
    removed
        .languageValue
        .toNormal()
}

@EsSpec("Map.prototype.entries")
private val mapEntries = method("entries") fn@ { thisArg, _ ->
    val map = thisArg.requireToBe<MapType> { return@fn it }
    createIteratorObjectFromSequence(
        map.map.entries
            .asSequence()
            .map { (key, value) ->
                ImmutableArrayType(listOf(key, value))
            }
    )
        .toNormal()
}

@EsSpec("Map.prototype.forEach")
private val mapForEach = method("forEach", 1u) fn@ { thisArg, args ->
    val map = thisArg.requireToBe<MapType> { return@fn it }
    val callback = args[0]
        .requireToBe<FunctionType> { return@fn it }
    map.map.forEach { (key, value) ->
        callback.call(null, listOf(value, key, map))
            .orReturn { return@fn it }
    }
    map.toNormal()
}

@EsSpec("Map.prototype.get")
private val mapGet = method("get", 1u) fn@ { thisArg, args ->
    val map = thisArg.requireToBe<MapType> { return@fn it }
    val key = args[0]
    map.map.entries.find { (k) -> sameValue(k, key) }
        ?.value
        .normalizeToNormal()
}

@EsSpec("Map.prototype.has")
private val mapHas = method("has", 1u) fn@ { thisArg, args ->
    val map = thisArg.requireToBe<MapType> { return@fn it }
    val key = args[0]
    (map.map.keys.firstOrNull { sameValue(it, key) } != null)
        .languageValue
        .toNormal()
}

@EsSpec("Map.prototype.keys")
private val mapKeys = method("keys") fn@ { thisArg, _ ->
    val map = thisArg.requireToBe<MapType> { return@fn it }
    createIteratorObjectFromSequence(map.map.keys.asSequence())
        .toNormal()
}

@EsSpec("Map.prototype.set")
private val mapSet = method("set", 2u) fn@ { thisArg, args ->
    val map = thisArg.requireToBe<MapType> { return@fn it }
    val key = args[0]
    val normalizedKey = when {
        key == NumberType.NEGATIVE_ZERO -> NumberType.POSITIVE_ZERO
        else -> key
    }
    val value = args[1]
    withUnsafeModification({ return@fn it }) {
        map.map[normalizedKey] = value
    }
    map.toNormal()
}

@EsSpec("Map.prototype.values")
private val mapValues = method("values") fn@ { thisArg, _ ->
    val map = thisArg.requireToBe<MapType> { return@fn it }
    createIteratorObjectFromSequence(map.map.values.asSequence())
        .toNormal()
}

@EsSpec("Map.prototype[@@iterator]")
private val mapIterator = method(SymbolType.WellKnown.iterator) fn@ { thisArg, _ ->
    mapEntries.call(thisArg, emptyList())
}

@EsSpec("Map.prototype.size")
private val mapCountGetter = getter("count") fn@ { thisArg ->
    val map = thisArg.requireToBe<MapType> { return@fn it }
    map.map.size
        .languageValue
        .toNormal()
}

@EsSpec("%Map%")
val Map = BuiltinClassType(
    "Map",
    Object,
    mutableMapOf(),
    mutableMapOf(
        sealedMethod(mapClear),
        sealedMethod(mapRemove),
        sealedMethod(mapEntries),
        sealedMethod(mapForEach),
        sealedMethod(mapGet),
        sealedMethod(mapHas),
        sealedMethod(mapKeys),
        sealedMethod(mapSet),
        sealedMethod(mapValues),
        sealedMethod(mapIterator),
        "count".accessor(getter=mapCountGetter),
    ),
    constructor ctor@ { _, args ->
        val source = args.getOptional(0)
        if (source != null) TODO()
        MapType(mutableMapOf())
            .toNormal()
    },
)
