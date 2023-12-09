package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

private val immutableArrayFrom = functionWithoutThis("from", 1u) fn@ { args ->
    val arrayLike = args[0]
    val mapFunc = args.getOptional(1)
        ?.requireToBe<FunctionType> { return@fn it }
    TODO()
}

private val isArray = functionWithoutThis("isArray", 1u) fn@ { args ->
    val value = args[0]
    value.isAnyArray()
        .languageValue
        .toNormal()
}

private val immutableArrayOf = functionWithoutThis("of") fn@ { args ->
    ImmutableArrayType(args)
        .toNormal()
}

private val immutableArrayAddAt = method("addAt", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val size = arr.array.size
    val index = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeIndexWithin(size + 1) { return@fn it }
    val values = args.subList(1, args.size)
    val new = ImmutableArrayType.from(
        arr.array.subList(0, index) + values + arr.array.subList(index, size)
    )
    new.toNormal()
}

private val immutableArrayAddFirst = method("addFirst") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val new = ImmutableArrayType.from(args + arr.array)
    new.toNormal()
}

private val immutableArrayAddLast = method("addLast") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val new = ImmutableArrayType.from(arr.array + args)
    new.toNormal()
}

internal val any = method("any") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args.getOptional(0)
        ?.requireToBe<FunctionType> { return@fn it }
        ?: return@fn arr.array.any()
            .languageValue
            .toNormal()
    val res = arr.array.anyIndexed { i, it ->
        callback.callCollectionPredicate(it, i, arr) { return@fn it }
    }
    res
        .languageValue
        .toNormal()
}
private inline fun <T> List<T>.anyIndexed(predicate: (Int, T) -> Boolean): Boolean {
    if (this.isEmpty()) return false
    forEachIndexed { i, it ->
        if (predicate(i, it)) return true
    }
    return false
}

@EsSpec("Array.prototype.at")
internal val arrayAt = method("at", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val index = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeRelativeIndex { return@fn it }
        .resolveRelativeIndex(arr.array.size)
        ?: return@fn normalNull
    arr.array.getOrNull(index).normalizeToNormal()
}

@EsSpec("Array.prototype.concat")
private val immutableArrayConcatenate = method("concatenate") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val res = arr.array.toMutableList()
    for (item in args) {
        if (item is ArrayType) res.addAll(item.array)
        else res.add(item)
    }
    ImmutableArrayType.from(res)
        .toNormal()
}

@EsSpec("Array.prototype.copyWithin")
private val copyWithin = method("copyWithin") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    TODO()
}

@EsSpec("Array.prototype.entries")
internal val immutableArrayEntries = method("entries") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ImmutableArrayType> { return@fn it }
    val entriesSequence = arr.array.asSequence().mapIndexed { i, it ->
        ImmutableArrayType(listOf(i.languageValue, it))
    }
    createIteratorObjectFromSequence(entriesSequence)
        .toNormal()
}

@EsSpec("Array.prototype.every")
internal val arrayEvery = method("every", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    arr.array.forEachIndexed { i, it ->
        val passed = callback.callCollectionPredicate(it, i, arr) { return@fn it }
        if (!passed) return@fn BooleanType.FALSE.toNormal()
    }
    BooleanType.TRUE
        .toNormal()
}

@EsSpec("Array.prototype.filter")
private val immutableArrayFilter = method("filter", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    val res = arr.array.filterIndexed { i, it ->
        callback.callCollectionPredicate(it, i, arr) { return@fn it }
    }
    ImmutableArrayType.from(res)
        .toNormal()
}

@EsSpec("Array.prototype.find")
internal val arrayFirst = method("first") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args.getOptional(0)
        ?.requireToBe<FunctionType> { return@fn it }
        ?: return@fn arr.array.firstOrNull().normalizeToNormal()
    val found = arr.array.firstIndexed { i, it ->
        callback.callCollectionPredicate(it, i, arr) { return@fn it }
    }
    found.normalizeToNormal()
}
/**
 * @see Iterable.find
 */
private inline fun <T> Iterable<T>.firstIndexed(predicate: (Int, T) -> Boolean): T? {
    forEachIndexed { i, it ->
        if (predicate(i, it)) return it
    }
    return null
}

@EsSpec("Array.prototype.findIndex")
internal val arrayFirstIndex = method("firstIndex") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args.getOptional(0)
        ?.requireToBe<FunctionType> { return@fn it }
        ?: return@fn (
            if (arr.array.isEmpty()) -1
            else 0
        )
            .languageValue
            .toNormal()
    val index = arr.array.indexOfFirstIndexed { i, it ->
        callback.callCollectionPredicate(it, i, arr) { return@fn it }
    }
    index
        .languageValue
        .toNormal()
}
/**
 * @see Iterable.indexOfFirst
 */
private inline fun <T> Iterable<T>.indexOfFirstIndexed(predicate: (Int, T) -> Boolean): Int {
    forEachIndexed { i, it ->
        if (predicate(i, it)) return i
    }
    return -1
}

@EsSpec("Array.prototype.flat")
private val immutableArrayFlat = method("flat") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val depth = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedIntOrPositiveInfinity { return@fn it }
        ?: 1
    val res = mutableListOf<LanguageType>()
    arr.array.forEach {
        res.addFlattened(it, depth)
    }
    ImmutableArrayType.from(res)
        .toNormal()
}
private fun MutableList<LanguageType>.addFlattened(source: LanguageType, depth: Int) {
    if (source !is ArrayType || depth == 0) {
        this += source
        return
    }

    source.array.forEach {
        addFlattened(it, depth - 1)
    }
}

@EsSpec("Array.prototype.flatMap")
private val immutableArrayFlatMap = method("flatMap", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    val new = ImmutableArrayType.from(
        arr.array.flatMapIndexed { i, it ->
            flatCallback(
                callback.callCollectionCallback(it, i, arr) { return@fn it }
            )
        }
    )
    new.toNormal()
}

@EsSpec("Array.prototype.forEach")
internal val arrayForEach = method("forEach", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    arr.array.forEachIndexed { i, it ->
        callback.callCollectionCallback(it, i, arr) { return@fn it }
    }
    arr.toNormal()
}

@EsSpec("Array.prototype.includes")
internal val arrayIncludes = method("includes", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val value = args[0]
    val startIndex = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedInt { return@fn it }
        ?: 0
    val res = arr.array.subList(startIndex, arr.array.size).any {
        sameValue(it, value, NumberType::internallyEqual)
    }
    res
        .languageValue
        .toNormal()
}

@EsSpec("Array.prototype.indexOf")
internal val arrayIndexOf = method("indexOf", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val value = args[0]
    val startIndex = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedInt { return@fn it }
        ?: 0
    val index = arr.array.subList(startIndex, arr.array.size)
        .indexOfFirst { sameValue(it, value, NumberType::internallyEqual) }
    index
        .languageValue
        .toNormal()
}

@EsSpec("Array.prototype.keys")
private val indices = method("indices") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val indicesSeq = arr.array.indices.asSequence()
        .map { it.languageValue }
    createIteratorObjectFromSequence(indicesSeq)
        .toNormal()
}

@EsSpec("Array.prototype.join")
internal val arrayJoin = method("join") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val separator = args.getOptional(0)
        ?.requireToBeString { return@fn it }
        ?: ", "
    val res = arr.array
        .map {
            stringify(it)
                .orReturn { return@fn it }
                .value
        }
        .joinToString(separator)
    res
        .languageValue
        .toNormal()
}

@EsSpec("Array.prototype.findLast")
internal val arrayLast = method("last") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args.getOptional(0)
        ?.requireToBe<FunctionType> { return@fn it }
        ?: return@fn arr.array.lastOrNull().normalizeToNormal()
    val found = arr.array.asReversed().firstIndexed { i, it ->
        callback.callCollectionPredicate(it, arr.array.lastIndex - i, arr) { return@fn it }
    }
    found.normalizeToNormal()
}

@EsSpec("Array.prototype.findLastIndex")
internal val arrayLastIndex = method("lastIndex") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    val index = arr.array.asReversed().indexOfFirstIndexed { i, it ->
        callback.callCollectionPredicate(it, arr.array.lastIndex - i, arr) { return@fn it }
    }
    index
        .languageValue
        .toNormal()
}

@EsSpec("Array.prototype.map")
private val immutableArrayMap = method("map", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    val res = arr.array.mapIndexed { i, it ->
        callback.callCollectionCallback(it, i, arr) { return@fn it }
    }
    ImmutableArrayType.from(res)
        .toNormal()
}

private inline fun FunctionType.callReduceCallback(
    accumulator: LanguageType,
    element: LanguageType,
    index: Int,
    collection: LanguageType,
    rtn: AbruptReturnLambda,
) =
    call(null, listOf(accumulator, element, index.languageValue, collection))
        .orReturn(rtn)
internal val reduceFromLeft = method("reduceFromLeft", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    val initial = args.getOrNull(1) // must not use `.getOptional()` because it will normalize null
    val res =
        if (initial == null) arr.array.reduceIndexed { i, acc, it ->
            callback.callReduceCallback(acc, it, i, arr) { return@fn it }
        }
        else arr.array.foldIndexed(initial) { i, acc, it ->
            callback.callReduceCallback(acc, it, i, arr) { return@fn it }
        }
    res.toNormal()
}

internal val reduceFromRight = method("reduceFromRight", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    val initial = args.getOrNull(1) // must not use `.getOptional()` because it will normalize null
    val res =
        if (initial == null) arr.array.reduceRightIndexed { i, acc, it ->
            callback.callReduceCallback(acc, it, i, arr) { return@fn it }
        }
        else arr.array.foldRightIndexed(initial) { i, acc, it ->
            callback.callReduceCallback(acc, it, i, arr) { return@fn it }
        }
    res.toNormal()
}

private val immutableArrayRemoveAt = method("removeAt", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val index = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeIndexWithin(arr.array.size + 1) { return@fn it }
    val count = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedInt { return@fn it }
        ?: 1
    val res =
        if (count == 0 && arr is ImmutableArrayType) arr
        else ImmutableArrayType.from(arr.array.take(index) + arr.array.drop(index + count))
    res.toNormal()
}

private val immutableArrayRemoveFirst = method("removeFirst") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val count = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedInt { return@fn it }
        ?: 1
    if (count == 0 && arr is ImmutableArrayType) return@fn arr.toNormal()
    val new = ImmutableArrayType.from(arr.array.drop(count))
    new.toNormal()
}

private val immutableArrayRemoveLast = method("removeLast") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val count = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedInt { return@fn it }
        ?: 1
    if (count == 0 && arr is ImmutableArrayType) return@fn arr.toNormal()
    val new = ImmutableArrayType.from(arr.array.dropLast(count))
    new.toNormal()
}

@EsSpec("Array.prototype.toReversed")
private val immutableArrayReverse = method("reverse") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val new = ImmutableArrayType.from(arr.array.reversed())
    new.toNormal()
}

@EsSpec("Array.prototype.slice")
private val immutableArraySlice = method("slice", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val size = arr.array.size
    val unsafeStart = args[0]
        .normalizeNull()
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRelativeIndex { return@fn it }
        ?.resolveRelativeIndex(size)
        ?: 0
    val unsafeEnd = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRelativeIndex { return@fn it }
        ?.resolveRelativeIndex(size)
        ?: size
    if (unsafeStart > unsafeEnd) return@fn throwError(RangeErrorKind.SLICE_START_GREATER_THAN_END)
    val start = unsafeStart.coerceIn(0, size)
    val end = unsafeEnd.coerceIn(0, size)
    val new = ImmutableArrayType.from(
        if (arr is ImmutableArrayType) arr.array.subList(start, end)
        else arr.array.slice(start until end)
    )
    new.toNormal()
}

@EsSpec("Array.prototype.toSorted")
private val immutableArraySort = method("sort") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val compareFn = args.getOptional(0)
        ?.requireToBe<FunctionType> { return@fn it }
        ?: sortDefaultCompareFn
    generalSort(compareFn) { comp ->
        ImmutableArrayType.from(arr.array.sortedWith(comp))
    }
}
internal val sortDefaultCompareFn = functionWithoutThis("compareFn", 2u) sort@ { args ->
    val a = args[0]
    val b = args[1]
    when {
        a.lessThan(b)
            .orReturn { return@sort it }
            .value
            -> -1
        b.lessThan(a)
            .orReturn { return@sort it }
            .value
            -> 1
        else -> 0
    }
        .languageValue
        .toNormal()
}
internal inline fun generalSort(
    compareFn: FunctionType,
    sort: (comparator: (LanguageType, LanguageType) -> Int) -> ArrayType,
): MaybeAbrupt<ArrayType> {
    val res: ArrayType
    try {
        res = sort { a, b ->
            val compareRes = compareFn.call(null, listOf(a, b))
                .orReturn { throw SortBreakException(it) }
                .requireToBe<NumberType> { throw SortBreakException(it) }
            when {
                compareRes.isNaN ->
                    throw SortBreakException(
                        throwError(TypeErrorKind.COMPARATOR_RETURNED_NAN)
                    )
                compareRes.isZero -> 0
                compareRes.isNegative -> -1
                else -> 1
            }
        }
    } catch (e: SortBreakException) {
        return e.abruptCompletion
    }
    return res.toNormal()
}
internal class SortBreakException(val abruptCompletion: Completion.Abrupt): Exception()

@EsSpec("Array.prototype.with")
private val immutableArraySet = method("set", 2u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val index = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeRelativeIndex { return@fn it }
        .resolveRelativeIndexOrReturn(arr.array.size) { return@fn it }
    val value = args[1]
    val before = arr.array.subList(0, index)
    val after = arr.array.subList(index + 1, arr.array.size)
    val new = ImmutableArrayType.from(before + listOf(value) + after)
    new.toNormal()
}

@EsSpec("Array.prototype.values")
private val immutableArrayValues = method("values") fn@ { thisArg, _ ->
    val arr = thisArg.requireToBe<ImmutableArrayType> { return@fn it }
    createIteratorObjectFromSequence(arr.array.asSequence())
        .toNormal()
}

@EsSpec("Array.prototype[@@iterator]")
private val immutableArrayIterator = method(SymbolType.WellKnown.iterator) fn@ { thisArg, _ ->
    immutableArrayValues.call(thisArg, emptyList())
}

/**
 * See [23.1.4.1 length](https://tc39.es/ecma262/multipage/indexed-collections.html#sec-properties-of-array-instances-length).
 */
@EsSpec("-")
internal val arrayCountGetter = getter("count") fn@ { thisArg ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    arr.array.size
        .languageValue
        .toNormal()
}

@EsSpec("%Array%")
val Array: BuiltinClassType = BuiltinClassType(
    "Array",
    Object,
    mutableMapOf(
        sealedMethod(immutableArrayFrom),
        sealedMethod(isArray),
        sealedMethod(immutableArrayOf),
    ),
    mutableMapOf(
        sealedMethod(immutableArrayAddAt),
        sealedMethod(immutableArrayAddFirst),
        sealedMethod(immutableArrayAddLast),
        sealedMethod(any),
        sealedMethod(arrayAt),
        sealedMethod(immutableArrayConcatenate),
        sealedMethod(immutableArrayEntries),
        sealedMethod(arrayEvery),
        sealedMethod(immutableArrayFilter),
        sealedMethod(arrayFirst),
        sealedMethod(arrayFirstIndex),
        sealedMethod(immutableArrayFlat),
        sealedMethod(immutableArrayFlatMap),
        sealedMethod(arrayForEach),
        sealedMethod(arrayIncludes),
        sealedMethod(arrayIndexOf),
        sealedMethod(indices),
        sealedMethod(arrayJoin),
        sealedMethod(arrayLast),
        sealedMethod(arrayLastIndex),
        sealedMethod(immutableArrayMap),
        sealedMethod(reduceFromLeft),
        sealedMethod(reduceFromRight),
        sealedMethod(immutableArrayRemoveAt),
        sealedMethod(immutableArrayRemoveFirst),
        sealedMethod(immutableArrayRemoveLast),
        sealedMethod(immutableArrayReverse),
        sealedMethod(immutableArraySlice),
        sealedMethod(immutableArraySort),
        sealedMethod(immutableArraySet),
        sealedMethod(immutableArrayIterator),
        "count".accessor(getter=arrayCountGetter),
    ),
    constructor ctor@ { _, args ->
        val count = args.getOptional(0)
            ?.requireToBe<NumberType> { return@ctor it }
            ?.requireToBeUnsignedInt { return@ctor it }
            ?: 0
        val arr = ImmutableArrayType(
            List(count) { NullType }
        )
        arr.toNormal()
    },
)

internal fun flatCallback(value: LanguageType) =
    if (value is ArrayType) value.array
    else listOf(value)
