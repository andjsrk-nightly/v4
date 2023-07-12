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
    val old = when (val value = args[0]) {
        is StringType -> value
        else ->
            value.getMethod(SymbolType.WellKnown.replace)
                ?.let {
                    val replaceMethod = returnIfAbrupt(it) { return@fn it }
                    returnIfAbrupt(checkNewArg(new)) { return@fn it }
                    return@fn replaceMethod._call(value, listOf(stringArg, new))
                }
                ?: return@fn unexpectedType(value, "${generalizedDescriptionOf<StringType>()} or a value that has Symbol.replace method")
    }
    returnIfAbrupt(checkNewArg(new)) { return@fn it }

    Completion.Normal(
        when (new) {
            is StringType ->
                // no special patterns supported since it can be replaced by passing a function as an argument
                string.replaceFirst(old.value, new.value).languageValue
            is FunctionType -> {
                val pos = string.indexOf(old.value)
                if (pos == -1) stringArg
                else {
                    val result = returnIfAbrupt(
                        new._call(null, listOf(old, pos.languageValue, stringArg))
                    ) { return@fn it }
                        .requireToBeString { return@fn it }
                    string.replaceFirst(old.value, result).languageValue
                }
            }
            else -> missingBranch()
        }
    )
}

private fun checkNewArg(value: LanguageType) =
    when (value) {
        is StringType, is FunctionType -> empty
        else -> unexpectedType(value, StringType::class, FunctionType::class)
    }
