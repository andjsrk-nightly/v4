package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.RangeErrorKind
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinClassType.Companion.constructor
import java.text.Normalizer

private const val REPLACEMENT_CHARACTER = '\uFFFD'

private val stringFrom = BuiltinFunctionType("from", 1u) fn@ { _, args ->
    val value = args[0]
    stringify(value)
}

private val fromCodePoint = BuiltinFunctionType("fromCodePoint") fn@ { _, args ->
    val builder = StringBuilder()
    for (arg in args) {
        val codePoint = arg
            .requireToBe<NumberType> { return@fn it }
            .requireToBeCodePoint { return@fn it }
        builder.appendCodePoint(codePoint)
    }
    Completion.Normal(
        builder.toString().languageValue
    )
}

private val fromCodeUnit = BuiltinFunctionType("fromCodeUnit") fn@ { _, args ->
    val builder = StringBuilder(args.size)
    for (arg in args) {
        val codeUnit = arg
            .requireToBe<NumberType> { return@fn it }
            .requireToBeIntegerWithin(Ranges.uint16, "A code unit") { return@fn it }
        builder.append(codeUnit.toChar())
    }
    Completion.Normal(
        builder.toString().languageValue
    )
}

private val stringAt = builtinMethod("at", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val index = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeRelativeIndex { return@fn it }
        .resolveRelativeIndex(string.length)
        ?: return@fn Completion.Normal.`null`
    Completion.Normal(
        string.getOrNull(index)?.toString()?.languageValue ?: NullType
    )
}

private val codePoint = builtinMethod("codePoint") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val index = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRelativeIndex { return@fn it }
        ?.run {
            resolveRelativeIndex(string.length) ?: return@fn Completion.Normal.`null`
        }
        ?: 0
    if (string.isEmpty()) return@fn Completion.Normal.`null`
    assert(index <= string.length)
    Completion.Normal(
        string.codePointAt(index).languageValue
    )
}

private val codeUnit = builtinMethod("codeUnit") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val index = args.getOptional(0)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRelativeIndex { return@fn it }
        ?.run {
            resolveRelativeIndex(string.length) ?: return@fn Completion.Normal.`null`
        }
        ?: 0
    if (string.isEmpty()) return@fn Completion.Normal.`null`
    assert(index <= string.length)
    Completion.Normal(
        string[index].code.languageValue
    )
}

private val concat = builtinMethod("concat") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val builder = StringBuilder(string)
    for (arg in args) {
        val str = arg.requireToBeString { return@fn it }
        builder.append(str)
    }
    Completion.Normal(
        builder.toString().languageValue
    )
}

private val endsWith = builtinMethod("endsWith", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val search = args[0].requireToBeString { return@fn it }
    val stringEnd = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeIndex { return@fn it }
        ?.coerceAtMost(string.length)
        ?: string.length
    Completion.Normal(
        string.dropLast(string.length - stringEnd)
            .endsWith(search)
            .languageValue
    )
}

private val findMatchedIndex = builtinMethod("findMatchedIndex", 1u) fn@ { thisArg, args ->
    val stringArg = thisArg.requireToBe<StringType> { return@fn it }
    val generalArg = args[0] // intentionally does not coerce to regular expressions
    val findMatchedIndexMethod = generalArg.getMethod(SymbolType.WellKnown.findMatchedIndex)
        ?.returnIfAbrupt { return@fn it }
        ?: return@fn unexpectedType(generalArg, "a value that has Symbol.findMatchedIndex method")
    findMatchedIndexMethod._call(generalArg, listOf(stringArg))
}

private val includes = builtinMethod("includes", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val search = args[0].requireToBeString { return@fn it }
    val startIndex = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeIndexWithinString(string) { return@fn it }
        ?: 0
    Completion.Normal(
        BooleanType.from(
            string.indexOf(search, startIndex) != -1
        )
    )
}

private val indexOf = builtinMethod("indexOf", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val search = args[0].requireToBeString { return@fn it }
    val startIndex = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeIndexWithinString(string) { return@fn it }
        ?: 0
    Completion.Normal(
        string.indexOf(search, startIndex).languageValue
    )
}

private val isWellFormed = builtinMethod("isWellFormed") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    for (codePoint in string.codePoints()) {
        if (codePoint.isUnpairedSurrogate()) return@fn Completion.Normal(BooleanType.FALSE)
    }
    Completion.Normal(BooleanType.TRUE)
}

private val iterator = builtinMethod(SymbolType.WellKnown.iterator) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    TODO()
}

private val lastIndexOf = builtinMethod("lastIndexOf", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val search = args[0].requireToBeString { return@fn it }
    val stringEnd = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBePositionWithinString(string) { return@fn it }
        ?: 0
    Completion.Normal(
        string.lastIndexOf(search, stringEnd).languageValue
    )
}

private val lengthGetter = AccessorProperty.builtinGetter("length") fn@ {
    val string = it.requireToBeString { return@fn it }
    Completion.Normal(
        string.length.languageValue
    )
}

private val localeCompare = builtinMethod("localeCompare", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val that = args[0].requireToBeString { return@fn it }
    TODO()
}

private val match = builtinMethod("match", 1u) fn@ { thisArg, args ->
    val stringArg = thisArg.requireToBe<StringType> { return@fn it }
    val generalArg = args[0]
    val matchMethod = generalArg.getMethod(SymbolType.WellKnown.match)
        ?.returnIfAbrupt { return@fn it }
        ?: return@fn unexpectedType(generalArg, "a value that has Symbol.match method")
    matchMethod._call(generalArg, listOf(stringArg, BooleanType.FALSE))
}

private val matchAll = builtinMethod("matchAll", 1u) fn@ { thisArg, args ->
    val stringArg = thisArg.requireToBe<StringType> { return@fn it }
    val generalArg = args[0]
    val matchMethod = generalArg.getMethod(SymbolType.WellKnown.match)
        ?.returnIfAbrupt { return@fn it }
        ?: return@fn unexpectedType(generalArg, "a value that has Symbol.match method")
    matchMethod._call(generalArg, listOf(stringArg, BooleanType.TRUE))
}

private val normalize = builtinMethod("normalize") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val form = args.getOptional(1)
        ?.requireToBeString { return@fn it }
        ?: "NFC"
    val normalized = Normalizer.normalize(string, Normalizer.Form.valueOf(form))
    Completion.Normal(
        normalized.languageValue
    )
}

private val padEnd = builtinMethod("padEnd", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val maxLength = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeIntegerWithin(Ranges.index, "maxLength") { return@fn it }
        .toInt()
    val fillString = args.getOptional(1)
        ?.requireToBeString { return@fn it }
        ?: " "
    val builder = StringBuilder()
    val remained = (maxLength - string.length).coerceAtLeast(0)
    val iterationCount = remained / fillString.length
    val extraCharCount = remained % fillString.length
    fillString.take(extraCharCount)
        .let {
            if (it.isNotEmpty()) builder.append(it)
        }
    repeat(iterationCount) {
        builder.append(fillString)
    }
    Completion.Normal(
        builder.toString().languageValue
    )
}

private val padStart = builtinMethod("padStart", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val maxLength = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeIntegerWithin(Ranges.index, "maxLength") { return@fn it }
        .toInt()
    val fillString = args.getOptional(1)
        ?.requireToBeString { return@fn it }
        ?: " "
    val builder = StringBuilder(string)
    val remained = (maxLength - string.length).coerceAtLeast(0)
    val iterationCount = remained / fillString.length
    val extraCharCount = remained % fillString.length
    repeat(iterationCount) {
        builder.append(fillString)
    }
    fillString.take(extraCharCount)
        .let {
            if (it.isNotEmpty()) builder.append(it)
        }
    Completion.Normal(
        builder.toString().languageValue
    )
}

private val repeat = builtinMethod("repeat", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val count = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeUnsignedInt { return@fn it }
    Completion.Normal(
        if (count == 0) StringType.empty
        else string.repeat(count).languageValue
    )
}

private val replaceAll = builtinMethod("replaceAll", 2u) fn@ { thisArg, args ->
    val stringArg = thisArg.requireToBe<StringType> { return@fn it }
    val string = stringArg.value
    val new = args[1]
    val oldArg = when (val value = args[0]) {
        is StringType -> value
        else -> return@fn (
                value.getMethod(SymbolType.WellKnown.replace)
                    ?.returnIfAbrupt { return@fn it }
                    ?.let { replaceMethod ->
                        checkStringReplaceNewArg(new)
                            .returnIfAbrupt { return@fn it }
                        replaceMethod._call(value, listOf(stringArg, new, BooleanType.TRUE))
                    }
                    ?: unexpectedType(value, "${generalizedDescriptionOf<StringType>()} or a value that has Symbol.replace method")
                )
    }
    val old = oldArg.value
    checkStringReplaceNewArg(new)
        .returnIfAbrupt { return@fn it }

    Completion.Normal(
        when (new) {
            is StringType -> string.replace(old, new.value)
            is FunctionType -> {
                val builder = StringBuilder()
                val step = old.length.coerceAtLeast(1)
                var lastMatchEndIndex = 0
                var i = 0
                while (i < string.length) {
                    if (string.substring(i).startsWith(old)) {
                        if (lastMatchEndIndex != i) {
                            // there is an additional string between matched strings
                            builder.append(string.substring(lastMatchEndIndex, i))
                        }
                        val result = new._call(null, listOf(oldArg, i.languageValue, stringArg))
                            .returnIfAbrupt { return@fn it }
                            .requireToBeString { return@fn it }
                        builder.append(result)
                        i += step
                        lastMatchEndIndex = i
                    } else i += 1
                }
                if (lastMatchEndIndex != i) builder.append(string.substring(lastMatchEndIndex, i))
                builder.toString()
            }
            else -> missingBranch()
        }
            .languageValue
    )
}

private val replaceFirst = builtinMethod("replaceFirst", 2u) fn@ { thisArg, args ->
    val stringArg = thisArg.requireToBe<StringType> { return@fn it }
    val string = stringArg.value
    val new = args[1]
    val oldArg = when (val value = args[0]) {
        is StringType -> value
        else -> return@fn (
                value.getMethod(SymbolType.WellKnown.replace)
                    ?.returnIfAbrupt { return@fn it }
                    ?.let { replaceMethod ->
                        checkStringReplaceNewArg(new)
                            .returnIfAbrupt { return@fn it }
                        replaceMethod._call(value, listOf(stringArg, new, BooleanType.FALSE))
                    }
                    ?: unexpectedType(value, "${generalizedDescriptionOf<StringType>()} or a value that has Symbol.replace method")
                )
    }
    val old = oldArg.value
    checkStringReplaceNewArg(new)
        .returnIfAbrupt { return@fn it }

    Completion.Normal(
        when (new) {
            is StringType ->
                // no special patterns supported since it can be replaced by passing a function as an argument
                string.replaceFirst(old, new.value).languageValue
            is FunctionType -> {
                val pos = string.indexOf(old)
                if (pos == -1) stringArg
                else {
                    val result = new._call(null, listOf(oldArg, pos.languageValue, stringArg))
                        .returnIfAbrupt { return@fn it }
                        .requireToBeString { return@fn it }
                    string.replaceFirst(old, result).languageValue
                }
            }
            else -> missingBranch()
        }
    )
}

private val slice = builtinMethod("slice", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val unsafeStart = args[0]
        .normalizeNull()
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRelativeIndex { return@fn it }
        ?.resolveRelativeIndex(string.length)
        ?: 0
    val unsafeEnd = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeRelativeIndex { return@fn it }
        ?.resolveRelativeIndex(string.length)
        ?: string.length
    if (unsafeStart > unsafeEnd) return@fn throwError(RangeErrorKind.SLICE_START_GREATER_THAN_END)
    val start = unsafeStart.coerceInString(string)
    val end = unsafeEnd.coerceInString(string)
    Completion.Normal(
        string.substring(start, end).languageValue
    )
}

private val sliceAbsolute = builtinMethod("sliceAbsolute", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val start = args[0]
        .normalizeNull()
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeIndex { return@fn it }
        ?.coerceInString(string)
        ?: 0
    val end = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeIndex { return@fn it }
        ?.coerceInString(string)
        ?: string.length
    if (start > end) return@fn throwError(RangeErrorKind.SLICE_START_GREATER_THAN_END)
    Completion.Normal(
        string.substring(start, end)
            .languageValue
    )
}

private val split = builtinMethod("split") fn@ { thisArg, args ->
    val stringArg = thisArg.requireToBe<StringType> { return@fn it }
    val string = stringArg.value
    val limit = args.getOptional(1)
    val separatorArg = when (val value = args.getOptional(0)) {
        is StringType -> value
        null -> {
            checkSplitLimitArg(limit)
                .returnIfAbrupt { return@fn it }
            return@fn Completion.Normal(
                ArrayType.from(
                    listOf(stringArg)
                )
            )
        }
        else -> {
            val splitMethod = value.getMethod(SymbolType.WellKnown.split)
                ?.returnIfAbrupt { return@fn it }
                ?: return@fn unexpectedType(value, "a value that has Symbol.split method")
            checkSplitLimitArg(limit)
                .returnIfAbrupt { return@fn it }
            return@fn splitMethod._call(value, listOf(stringArg, limit ?: NullType))
        }
    }
    val separator = separatorArg.value
    val safeLimit = checkSplitLimitArg(limit)
        .returnIfAbrupt { return@fn it }
        .value
    val res = string.split(separator, limit=safeLimit)
        .let {
            // NOTE 1 (remove leading/trailing empty strings if separator is an empty string)
            if (separator.isEmpty()) it.drop(1).dropLast(1)
            else it
        }
    Completion.Normal(
        ArrayType.from(
            res.map { it.languageValue }
        )
    )
}

private val startsWith = builtinMethod("startsWith", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val search = args[0].requireToBeString { return@fn it }
    val startIndex = args.getOptional(1)
        ?.requireToBe<NumberType> { return@fn it }
        ?.requireToBeIndex { return@fn it }
        ?: 0
    Completion.Normal(
        string.startsWith(search, startIndex)
            .languageValue
    )
}

private val toLocaleLowerCase = builtinMethod("toLocaleLowerCase") fn@ { thisArg, _ ->
    val string = thisArg.requireToBeString { return@fn it }
    TODO()
}

private val toLocaleUpperCase = builtinMethod("toLocaleUpperCase") fn@ { thisArg, _ ->
    val string = thisArg.requireToBeString { return@fn it }
    TODO()
}

private val toLowerCase = builtinMethod("toLowerCase") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    Completion.Normal(
        string.lowercase()
            .languageValue
    )
}

private val toString = builtinMethod(SymbolType.WellKnown.toString) fn@ { thisArg, _ ->
    thisArg.requireToBe<StringType> { return@fn it }
    Completion.Normal(thisArg)
}

private val toUpperCase = builtinMethod("toUpperCase") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    Completion.Normal(
        string.uppercase()
            .languageValue
    )
}

private val toWellFormed = builtinMethod("toWellFormed") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val builder = StringBuilder()
    for (codePoint in string.codePoints()) {
        if (codePoint.isUnpairedSurrogate()) builder.append(REPLACEMENT_CHARACTER)
        else builder.appendCodePoint(codePoint)
    }
    Completion.Normal(
        builder.toString().languageValue
    )
}

private val trim = builtinMethod("trim") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    Completion.Normal(
        string.trim().languageValue
    )
}

private val trimEnd = builtinMethod("trimEnd") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    Completion.Normal(
        string.trimEnd().languageValue
    )
}

private val trimStart = builtinMethod("trimStart") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    Completion.Normal(
        string.trimStart().languageValue
    )
}

@EsSpec("%String%")
val String = BuiltinClassType(
    "String",
    Object,
    mutableMapOf(
        "from".sealedData(stringFrom),
        sealedData(::fromCodePoint),
        sealedData(::fromCodeUnit),
        // TODO
    ),
    mutableMapOf(
        "at".sealedData(stringAt),
        sealedData(::codePoint),
        sealedData(::codeUnit),
        sealedData(::concat),
        sealedData(::endsWith),
        sealedData(::findMatchedIndex),
        sealedData(::includes),
        sealedData(::indexOf),
        sealedData(::isWellFormed),
        sealedData(::lastIndexOf),
        sealedData(::localeCompare),
        sealedData(::match),
        sealedData(::matchAll),
        sealedData(::normalize),
        sealedData(::padEnd),
        sealedData(::padStart),
        sealedData(::repeat),
        sealedData(::replaceAll),
        sealedData(::replaceFirst),
        sealedData(::slice),
        sealedData(::sliceAbsolute),
        sealedData(::split),
        sealedData(::startsWith),
        sealedData(::toLocaleLowerCase),
        sealedData(::toLocaleUpperCase),
        sealedData(::toLowerCase),
        SymbolType.WellKnown.toString.sealedData(toString),
        sealedData(::toUpperCase),
        sealedData(::toUpperCase),
        sealedData(::toWellFormed),
        sealedData(::trim),
        sealedData(::trimEnd),
        sealedData(::trimStart),
        SymbolType.WellKnown.iterator.sealedData(iterator),
        "length".accessor(lengthGetter),
        // TODO
    ),
    constructor { _, _ ->
        throwError(TypeErrorKind.CANNOT_CONSTRUCT, "String")
    },
)

private fun Int.coerceInString(string: String) =
    coerceIn(0, string.length)

private fun checkStringReplaceNewArg(value: LanguageType) =
    when (value) {
        is StringType, is FunctionType -> empty
        else -> unexpectedType(value, StringType::class, FunctionType::class)
    }
private fun checkSplitLimitArg(limit: LanguageType?): MaybeAbrupt<GeneralSpecValue<Int>> {
    return Completion.WideNormal(
        GeneralSpecValue(
            limit
                .requireToBe<NumberType> { return it }
                .requireToBeUnsignedInt { return it }
        )
    )
}

private inline fun NumberType.requireToBeCodePoint(`return`: AbruptReturnLambda): CodePoint =
    requireToBeIntegerWithin(Ranges.codePoint, "A code point", `return`)
        .toInt()
private inline fun NumberType.requireToBeIndexWithinString(string: String, name: String = "startIndex", `return`: AbruptReturnLambda): Int {
    val index = this.requireToBeIndex(`return`)
    if (index >= string.length) `return`(
        throwError(
            RangeErrorKind.MUST_BE_INTEGER_IN_RANGE,
            name,
            "0",
            "(length of the string) - 1",
        )
    )
    return index
}
private inline fun NumberType.requireToBePositionWithinString(string: String, name: String = "stringEnd", `return`: AbruptReturnLambda): Int {
    val index = this.requireToBeUnsignedInt(`return`)
    if (index > string.length) `return`(
        throwError(
            RangeErrorKind.MUST_BE_INTEGER_IN_RANGE,
            name,
            "0",
            "length of the string",
        )
    )
    return index
}

private typealias CodePoint = Int

private fun CodePoint.isUnpairedSurrogate(): Boolean {
    val chars = this.toChars()
    val first = chars[0]
    if (first.not { isSurrogate() }) return false
    if (first.isLowSurrogate()) return true
    val second = chars.getOrNull(1) ?: return true
    return second.not { isLowSurrogate() }
}

private fun CodePoint.toChars() =
    Character.toChars(this)
