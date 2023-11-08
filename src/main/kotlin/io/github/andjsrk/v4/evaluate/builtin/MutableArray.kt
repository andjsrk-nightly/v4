package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Ref
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal

private val mutableArrayFrom = functionWithoutThis("from", 1u) fn@ { args ->
    val arrayLike = args[0]
    val mapFunc = args.getOptional(1)
        ?.requireToBe<FunctionType> { return@fn it }
    TODO()
}

private val mutableArrayOf = functionWithoutThis("of") fn@ { args ->
    MutableArrayType(args.toMutableList())
        .toNormal()
}

@EsSpec("Array.prototype.splice")
private val mutableArrayAddAt = method("addAt", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    val index = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeIndexWithin(arr.array.size + 1/* allow length */) { return@fn it }
    val values = args.subList(1, args.size)
    withUnsafeModification({ return@fn it }) {
        arr.array.addAll(index, values)
    }
    arr.toNormal()
}

@EsSpec("Array.prototype.unshift")
private val mutableArrayAddFirst = method("addFirst") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    withUnsafeModification({ return@fn it }) {
        arr.array.addAll(0, args)
    }
    arr.toNormal()
}

@EsSpec("Array.prototype.push")
private val mutableArrayAddLast = method("addLast") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    withUnsafeModification({ return@fn it }) {
        arr.array.addAll(args)
    }
    arr.toNormal()
}

private val mutableArrayConcatenate = method("concatenate") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    withUnsafeModification({ return@fn it }) {
        args.forEach {
            if (it is ArrayType) arr.array.addAll(it.array)
            else arr.array.add(it)
        }
    }
    arr.toNormal()
}

private val mutableArrayFilter = method("filter", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    var i = 0
    withUnsafeModification({ return@fn it }) {
        while (i < arr.array.size) {
            val value = arr.array[i]
            val passed = callback.callCollectionPredicate(value, i, arr) { return@fn it }
            if (!passed) {
                arr.array.removeAt(i)
                continue // skip i += 1, because original element on the index is no longer available
            }
            i += 1
        }
    }
    arr.toNormal()
}

private val mutableArrayFlat = method("flat") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    val depth = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedIntOrPositiveInfinity { return@fn it }
        ?: 1
    val i = Ref(0)
    while (i.value < arr.array.size) {
        val elem = arr.array[i.value]
        arr.array.removeAt(i.value)
        arr.array.addFlattenedAt(i, elem, depth)
    }
    arr.toNormal()
}
private fun MutableList<LanguageType>.addFlattenedAt(
    index: Ref<Int>,
    source: LanguageType,
    depth: Int,
) {
    if (source !is ArrayType/* we are not interested in non array values */ || depth == 0) {
        add(index.value, source)
        index.value++
        return
    }

    if (source.array.isNotEmpty()) {
        source.array.forEach {
            addFlattenedAt(index, it, depth - 1)
        }
    }
}

private val mutableArrayFlatMap = method("flatMap", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    val i = Ref(0)
    while (i.value < arr.array.size) {
        val value = arr.array[i.value]
        val res = callback.call(null, listOf(value, i.value.languageValue, arr))
            .orReturn { return@fn it }
        arr.array.removeAt(i.value)
        arr.array.addFlattenedAt(i, res, 1)
    }
    arr.toNormal()
}

private val mutableArrayMap = method("map", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    repeat(arr.array.size) { i ->
        arr.array[i] = callback.call(null, listOf(arr.array[i], i.languageValue, arr))
            .orReturn { return@fn it }
    }
    arr.toNormal()
}

@EsSpec("Array.prototype.splice")
private val mutableArrayRemoveAt = method("removeAt", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    val index = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeIndexWithin(arr.array.size + 1) { return@fn it }
    val count = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedInt { return@fn it }
        ?: 1
    when (count) {
        0 -> {}
        1 -> {
            arr.array.removeAt(index)
        }
        else -> {
            val before = arr.array.take(index)
            val after = arr.array.drop(index + count)
            arr.array.clear()
            arr.array.addAll(before)
            arr.array.addAll(after)
        }
    }
    arr.toNormal()
}

@EsSpec("Array.prototype.shift")
private val mutableArrayRemoveFirst = method("removeFirst") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    val count = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedInt { return@fn it }
        ?: 1
    val rest = arr.array.drop(count)
    arr.array.clear()
    arr.array.addAll(rest)
    arr.toNormal()
}

@EsSpec("Array.prototype.pop")
private val mutableArrayRemoveLast = method("removeLast") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    val count = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedInt { return@fn it }
        ?: 1
    repeat(count) { arr.array.removeLastOrNull() }
    arr.toNormal()
}

@EsSpec("Array.prototype.reverse")
private val mutableArrayReverse = method("reverse") fn@ { thisArg, _ ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    arr.array.reverse()
    arr.toNormal()
}

private val mutableArraySet = method("set", 2u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    val index = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeRelativeIndex { return@fn it }
        .resolveRelativeIndexOrReturn(arr.array.size) { return@fn it }
    val value = args[1]
    arr.array[index] = value
    arr.toNormal()
}

private val mutableArraySlice = method("slice", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    val start = args[0]
        .requireToBe<NumberType> { return@fn it }
    TODO()
}

@EsSpec("Array.prototype.sort")
private val mutableArraySort = method("sort") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<MutableArrayType> { return@fn it }
    val compareFn = args.getOptional(0)
        ?.requireToBe<FunctionType> { return@fn it }
        ?: sortDefaultCompareFn
    generalSort(compareFn) { comp ->
        arr.array.sortWith(comp)
        arr
    }
}

val MutableArray = BuiltinClassType(
    "MutableArray",
    Object,
    mutableMapOf(
        sealedMethod(mutableArrayFrom),
        sealedMethod(mutableArrayOf),
    ),
    mutableMapOf(
        sealedMethod(mutableArrayAddAt),
        sealedMethod(mutableArrayAddFirst),
        sealedMethod(mutableArrayAddLast),
        sealedMethod(any),
        sealedMethod(arrayAt),
        sealedMethod(mutableArrayConcatenate),
        sealedMethod(arrayEvery),
        sealedMethod(mutableArrayFilter),
        sealedMethod(arrayFirst),
        sealedMethod(arrayFirstIndex),
        sealedMethod(mutableArrayFlat),
        sealedMethod(mutableArrayFlatMap),
        sealedMethod(arrayForEach),
        sealedMethod(arrayIncludes),
        sealedMethod(arrayIndexOf),
        sealedMethod(arrayJoin),
        sealedMethod(arrayLast),
        sealedMethod(arrayLastIndex),
        sealedMethod(mutableArrayMap),
        sealedMethod(reduceFromLeft),
        sealedMethod(reduceFromRight),
        sealedMethod(mutableArrayRemoveAt),
        sealedMethod(mutableArrayRemoveFirst),
        sealedMethod(mutableArrayRemoveLast),
        sealedMethod(mutableArrayReverse),
        sealedMethod(mutableArraySet),
        sealedMethod(mutableArraySlice),
        sealedMethod(mutableArraySort),
        "count".accessor(getter=arrayCountGetter),
    ),
    constructor ctor@ { _, args ->
        val size = args.getOptional(0)
            ?.requireToBe<NumberType> { return@ctor it }
            ?.requireToBeUnsignedInt { return@ctor it }
            ?: 0
        val arr = MutableArrayType(MutableList(size) { NullType })
        arr.toNormal()
    },
)
