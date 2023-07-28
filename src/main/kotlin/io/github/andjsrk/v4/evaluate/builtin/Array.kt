package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.AccessorProperty
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor

private val immutableArrayFrom = BuiltinFunctionType("from", 1u) fn@ { _, args ->
    val arrayLike = args[0]
    val mapFunc = args.getOptional(1)
        ?.requireToBe<FunctionType> { return@fn it }
    TODO()
}

private val isArray = BuiltinFunctionType("isArray", 1u) fn@ { _, args ->
    val value = args[0]
    Completion.Normal(
        BooleanType.from(value is ArrayType)
    )
}

private val immutableArrayOf = BuiltinFunctionType("of") fn@ { _, args ->
    Completion.Normal(
        ImmutableArrayType(args)
    )
}

private val immutableArrayAddAt = builtinMethod("addAt", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val size = arr.array.size
    val index = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeIndexWithin(size + 1) { return@fn it }
    val values = args.subList(1, args.size)
    val new = ImmutableArrayType.from(
        arr.array.subList(0, index) + values + arr.array.subList(index + 1, size)
    )
    Completion.Normal(new)
}

private val immutableArrayAddFirst = builtinMethod("addFirst") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val new = ImmutableArrayType.from(args + arr.array)
    Completion.Normal(new)
}

private val immutableArrayAddLast = builtinMethod("addLast") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val new = ImmutableArrayType.from(arr.array + args)
    Completion.Normal(new)
}

internal val any = builtinMethod("any") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args.getOptional(0)
        ?.requireToBe<FunctionType> { return@fn it }
        ?: return@fn Completion.Normal(
            arr.array.any()
                .languageValue
        )
    val res = arr.array.anyIndexed { i, it ->
        callback.callPredicate(it, i, arr) { return@fn it }
    }
    Completion.Normal(res.languageValue)
}
private inline fun <T> List<T>.anyIndexed(predicate: (Int, T) -> Boolean): Boolean {
    if (this.isEmpty()) return false
    forEachIndexed { i, it ->
        if (predicate(i, it)) return true
    }
    return false
}

@EsSpec("Array.prototype.at")
internal val arrayAt = builtinMethod("at", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val index = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeRelativeIndex { return@fn it }
        .resolveRelativeIndex(arr.array.size)
        ?: return@fn Completion.Normal.`null`
    Completion.Normal(
        arr.array.getOrNull(index) ?: NullType
    )
}

@EsSpec("Array.prototype.concat")
private val immutableArrayConcatenate = builtinMethod("concatenate") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val res = arr.array.toMutableList()
    for (item in args) {
        if (item is ArrayType) res.addAll(item.array)
        else res.add(item)
    }
    Completion.Normal(
        ImmutableArrayType.from(res)
    )
}

@EsSpec("Array.prototype.copyWithin")
private val copyWithin = builtinMethod("copyWithin") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    TODO()
}

@EsSpec("Array.prototype.entries")
internal val arrayEntries = builtinMethod("entries") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    TODO()
}

@EsSpec("Array.prototype.every")
internal val arrayEvery = builtinMethod("every", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    arr.array.forEachIndexed { i, it ->
        val passed = callback.callPredicate(it, i, arr) { return@fn it }
        if (!passed) return@fn Completion.Normal(BooleanType.FALSE)
    }
    Completion.Normal(BooleanType.TRUE)
}

@EsSpec("Array.prototype.filter")
private val immutableArrayFilter = builtinMethod("filter", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    val res = arr.array.filterIndexed { i, it ->
        callback.callPredicate(it, i, arr) { return@fn it }
    }
    Completion.Normal(
        ImmutableArrayType.from(res)
    )
}

@EsSpec("Array.prototype.find")
internal val arrayFirst = builtinMethod("first") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args.getOptional(0)
        ?.requireToBe<FunctionType> { return@fn it }
        ?: return@fn Completion.Normal(arr.array.firstOrNull() ?: NullType)
    val found = arr.array.firstIndexed { i, it ->
        callback.callPredicate(it, i, arr) { return@fn it }
    }
    Completion.Normal(found ?: NullType)
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
internal val arrayFirstIndex = builtinMethod("firstIndex") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args.getOptional(0)
        ?.requireToBe<FunctionType> { return@fn it }
        ?: return@fn Completion.Normal(
            (
                if (arr.array.isEmpty()) -1
                else 0
            ).languageValue
        )
    val index = arr.array.indexOfFirstIndexed { i, it ->
        callback.callPredicate(it, i, arr) { return@fn it }
    }
    Completion.Normal(index.languageValue)
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
private val immutableArrayFlat = builtinMethod("flat") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val depth = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedInt { return@fn it }
        ?: 1
    val res = mutableListOf<LanguageType>()
    arr.array.forEach { addFlattened(res, it, depth) }
    Completion.Normal(
        ImmutableArrayType.from(res)
    )
}
private fun addFlattened(target: MutableList<LanguageType>, source: LanguageType, depth: Int) {
    if (source is ArrayType && depth > 0) source.array.forEach { addFlattened(target, it, depth - 1) }
    else target += source
}

@EsSpec("Array.prototype.flatMap")
private val immutableArrayFlatMap = builtinMethod("flatMap", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    val new = ImmutableArrayType.from(
        arr.array.flatMapIndexed { i, it ->
            flatCallback(
                callback._call(null, listOf(it, i.languageValue, arr))
                    .returnIfAbrupt { return@fn it }
            )
        }
    )
    Completion.Normal(new)
}

@EsSpec("Array.prototype.forEach")
internal val arrayForEach = builtinMethod("forEach", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    arr.array.forEachIndexed { i, it ->
        callback._call(null, listOf(it, i.languageValue, arr))
            .returnIfAbrupt { return@fn it }
    }
    Completion.Normal(arr)
}

@EsSpec("Array.prototype.includes")
internal val arrayIncludes = builtinMethod("includes", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val value = args[0]
    val startIndex = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedInt { return@fn it }
        ?: 0
    val res = arr.array.subList(startIndex, arr.array.size).any {
        sameValue(it, value, NumberType::internallyEqual)
    }
    Completion.Normal(res.languageValue)
}

@EsSpec("Array.prototype.indexOf")
internal val arrayIndexOf = builtinMethod("indexOf", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val value = args[0]
    val startIndex = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedInt { return@fn it }
        ?: 0
    val index = arr.array.subList(startIndex, arr.array.size)
        .indexOfFirst { sameValue(it, value, NumberType::internallyEqual) }
    Completion.Normal(index.languageValue)
}

@EsSpec("Array.prototype.join")
internal val arrayJoin = builtinMethod("join") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val separator = args.getOptional(0)
        ?.requireToBeString { return@fn it }
        ?: ", "
    val res = arr.array
        .map {
            stringify(it)
                .returnIfAbrupt { return@fn it }
                .value
        }
        .joinToString(separator)
    Completion.Normal(res.languageValue)
}

@EsSpec("Array.prototype.keys")
private val keys = builtinMethod("keys") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    TODO()
}

@EsSpec("Array.prototype.findLast")
internal val arrayLast = builtinMethod("last") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args.getOptional(0)
        ?.requireToBe<FunctionType> { return@fn it }
        ?: return@fn Completion.Normal(arr.array.lastOrNull() ?: NullType)
    val found = arr.array.asReversed().firstIndexed { i, it ->
        callback.callPredicate(it, i, arr) { return@fn it }
    }
    Completion.Normal(found ?: NullType)
}

@EsSpec("Array.prototype.findLastIndex")
internal val arrayLastIndex = builtinMethod("lastIndex") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    val index = arr.array.asReversed().indexOfFirstIndexed { i, it ->
        callback.callPredicate(it, i, arr) { return@fn it }
    }
    Completion.Normal(index.languageValue)
}

@EsSpec("Array.prototype.map")
private val immutableArrayMap = builtinMethod("map", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    val res = arr.array.mapIndexed { i, it ->
        callback._call(null, listOf(it, i.languageValue, arr))
            .returnIfAbrupt { return@fn it }
    }
    Completion.Normal(
        ImmutableArrayType.from(res)
    )
}

internal val reduceFromLeft = builtinMethod("reduceFromLeft", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    val initial = args.getOrNull(1) // must not use `.getOptional()` because it will normalize null
    val res =
        if (initial == null) arr.array.reduceIndexed { i, acc, it ->
            callback._call(null, listOf(acc, it, i.languageValue, arr))
                .returnIfAbrupt { return@fn it }
        }
        else arr.array.foldIndexed(initial) { i, acc, it ->
            callback._call(null, listOf(acc, it, i.languageValue, arr))
                .returnIfAbrupt { return@fn it }
        }
    Completion.Normal(res)
}

internal val reduceFromRight = builtinMethod("reduceFromRight", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val callback = args[0].requireToBe<FunctionType> { return@fn it }
    val initial = args.getOrNull(1) // must not use `.getOptional()` because it will normalize null
    val res =
        if (initial == null) arr.array.reduceRightIndexed { i, acc, it ->
            callback._call(null, listOf(acc, it, i.languageValue, arr))
                .returnIfAbrupt { return@fn it }
        }
        else arr.array.foldRightIndexed(initial) { i, acc, it ->
            callback._call(null, listOf(acc, it, i.languageValue, arr))
                .returnIfAbrupt { return@fn it }
        }
    Completion.Normal(res)
}

private val immutableArrayRemoveAt = builtinMethod("removeAt", 1u) fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val index = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeIndexWithin(arr.array.size + 1) { return@fn it }
    val count = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedInt { return@fn it }
        ?: 1
    val res =
        if (count == 0) arr
        else ImmutableArrayType.from(
            arr.array.take(index) + arr.array.drop(index + count)
        )
    Completion.Normal(arr)
}

private val immutableArrayRemoveFirst = builtinMethod("removeFirst") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val count = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedInt { return@fn it }
        ?: 1
    if (count == 0) return@fn Completion.Normal(arr)
    val new = ImmutableArrayType.from(arr.array.drop(count))
    Completion.Normal(new)
}

private val immutableArrayRemoveLast = builtinMethod("removeLast") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val count = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeUnsignedInt { return@fn it }
        ?: 1
    if (count == 0) return@fn Completion.Normal(arr)
    val new = ImmutableArrayType.from(arr.array.dropLast(count))
    Completion.Normal(new)
}

private val immutableArrayReverse = builtinMethod("reverse") fn@ { thisArg, args ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    val new = ImmutableArrayType.from(arr.array.reversed())
    Completion.Normal(new)
}

@EsSpec("Array.prototype.slice")
private val immutableArraySlice = builtinMethod("slice", 1u) fn@ { thisArg, args ->
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
        else arr.array.slice(start..end)
    )
    Completion.Normal(new)
}

/**
 * See [23.1.4.1 length](https://tc39.es/ecma262/multipage/indexed-collections.html#sec-properties-of-array-instances-length).
 */
@EsSpec("-")
internal val arrayLengthGetter = AccessorProperty.builtinGetter("length") fn@ { thisArg ->
    val arr = thisArg.requireToBe<ArrayType> { return@fn it }
    Completion.Normal(
        arr.array.size.languageValue
    )
}

@EsSpec("%Array%")
val Array = BuiltinClassType(
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
        sealedMethod(arrayEvery),
        sealedMethod(immutableArrayFilter),
        sealedMethod(arrayFirst),
        sealedMethod(arrayFirstIndex),
        sealedMethod(immutableArrayFlat),
        sealedMethod(immutableArrayFlatMap),
        sealedMethod(arrayForEach),
        sealedMethod(arrayIncludes),
        sealedMethod(arrayIndexOf),
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
        "length".accessor(getter=arrayLengthGetter),
    ),
    constructor ctor@ { _, args ->
        val size = args.getOptional(0)
            ?.requireToBe<NumberType> { return@ctor it }
            ?.requireToBeUnsignedInt { return@ctor it }
            ?: 0
        val mapFn = args.getOptional(1)
            ?.requireToBe<FunctionType> { return@ctor it }
        val arr = ImmutableArrayType(
            List(size) { i ->
                mapFn?._call(null, listOf(i.languageValue))
                    ?.returnIfAbrupt { return@ctor it }
                    ?: NullType
            }
        )
        Completion.Normal(arr)
    },
)

internal inline fun FunctionType.callPredicate(
    element: LanguageType,
    index: Int,
    array: ArrayType,
    `return`: AbruptReturnLambda,
) =
    callAndRequireToBe<BooleanType>(null, listOf(element, index.languageValue, array), `return`)
        .value

internal fun flatCallback(value: LanguageType) =
    if (value is ArrayType) value.array
    else listOf(value)
