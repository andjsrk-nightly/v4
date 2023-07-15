package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

val split = builtinMethod("split") fn@ { thisArg, args ->
    val stringArg = thisArg.requireToBe<StringType> { return@fn it }
    val string = stringArg.value
    val limit = args.getOptional(1)
    val separatorArg = when (val value = args.getOptional(0)) {
        is StringType -> value
        null -> {
            checkLimitArg(limit)
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
            checkLimitArg(limit)
                .returnIfAbrupt { return@fn it }
            return@fn splitMethod._call(value, listOf(stringArg, limit ?: NullType))
        }
    }
    val separator = separatorArg.value
    val safeLimit = checkLimitArg(limit)
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

private fun checkLimitArg(limit: LanguageType?): MaybeAbrupt<GeneralSpecValue<Int>> {
    return Completion.WideNormal(
        GeneralSpecValue(
            limit
                .requireToBe<NumberType> { return it }
                .requireToBeUnsignedInt { return it }
        )
    )
}
