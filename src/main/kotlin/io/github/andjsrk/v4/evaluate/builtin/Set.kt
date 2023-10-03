package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("Set.prototype.add")
private val setAdd = method("add", 1u) fn@ { thisArg, args ->
    val set = thisArg.requireToBe<SetType> { return@fn it }
    val newValues = args.filter { arg ->
        set.set.firstOrNull { sameValue(it, arg) } == null
    }
    withUnsafeModification({ return@fn it }) {
        set.set.addAll(newValues)
    }
    set.toNormal()
}

@EsSpec("Set.prototype.clear")
private val setClear = method("clear") fn@ { thisArg, _ ->
    val set = thisArg.requireToBe<SetType> { return@fn it }
    withUnsafeModification({ return@fn it }) {
        set.set.clear()
    }
    normalNull
}

@EsSpec("Set.prototype.delete")
private val setDelete = method("delete", 1u) fn@ { thisArg, args ->
    val set = thisArg.requireToBe<SetType> { return@fn it }
    val value = args[0]
    var deleted = false
    withUnsafeModification({ return@fn it }) {
        deleted = set.set.removeIf { sameValue(it, value) }
    }
    deleted
        .languageValue
        .toNormal()
}

@EsSpec("Set.prototype.forEach")
private val setForEach = method("forEach", 1u) fn@ { thisArg, args ->
    val set = thisArg.requireToBe<SetType> { return@fn it }
    val callback = args[0]
        .requireToBe<FunctionType> { return@fn it }
    set.set.forEach {
        callback.call(null, listOf(it, set))
            .orReturn { return@fn it }
    }
    set.toNormal()
}

@EsSpec("Set.prototype.has")
private val setHas = method("has", 1u) fn@ { thisArg, args ->
    val set = thisArg.requireToBe<SetType> { return@fn it }
    val value = args[0]
    (set.set.firstOrNull { sameValue(it, value) } != null)
        .languageValue
        .toNormal()
}

@EsSpec("Set.prototype.values")
private val setValues = method("values") fn@ { thisArg, _ ->
    val set = thisArg.requireToBe<SetType> { return@fn it }
    createIteratorObjectFromSequence(set.set.asSequence())
        .toNormal()
}

@EsSpec("Set.prototype[@@iterator]")
private val setIterator = method(SymbolType.WellKnown.iterator) fn@ { thisArg, _ ->
    setValues.call(thisArg, emptyList())
}

@EsSpec("Set.prototype.size")
private val setSizeGetter = getter("size") fn@ { thisArg ->
    val set = thisArg.requireToBe<SetType> { return@fn it }
    set.set.size
        .languageValue
        .toNormal()
}

@EsSpec("%Set%")
val Set = BuiltinClassType(
    "Set",
    Object,
    mutableMapOf(),
    mutableMapOf(
        sealedMethod(setAdd),
        sealedMethod(setClear),
        sealedMethod(setDelete),
        sealedMethod(setForEach),
        sealedMethod(setHas),
        sealedMethod(setValues),
        sealedMethod(setIterator),
        "size".accessor(getter=setSizeGetter),
    ),
    constructor ctor@ { _, args ->
        val source = args.getOptional(0)
        if (source != null) TODO()
        SetType(mutableSetOf())
            .toNormal()
    },
)
