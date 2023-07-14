package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.missingBranch

val replaceFirst = builtinMethod("replaceFirst", 2u) fn@ { thisArg, args ->
    val stringArg = thisArg.requireToBe<StringType> { return@fn it }
    val string = stringArg.value
    val new = args[1]
    val oldArg = when (val value = args[0]) {
        is StringType -> value
        else -> return@fn (
            value.getMethod(SymbolType.WellKnown.replace)
                ?.returnIfAbrupt { return@fn it }
                ?.let { replaceMethod ->
                    checkNewArg(new)
                        .returnIfAbrupt { return@fn it }
                    replaceMethod._call(value, listOf(stringArg, new, BooleanType.FALSE))
                }
                ?: unexpectedType(value, "${generalizedDescriptionOf<StringType>()} or a value that has Symbol.replace method")
        )
    }
    val old = oldArg.value
    checkNewArg(new)
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

internal fun checkNewArg(value: LanguageType) =
    when (value) {
        is StringType, is FunctionType -> empty
        else -> unexpectedType(value, StringType::class, FunctionType::class)
    }
