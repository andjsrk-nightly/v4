package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.missingBranch

val replaceAll = builtinMethod("replaceAll", 2u) fn@ { thisArg, args ->
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
                    replaceMethod._call(value, listOf(stringArg, new, BooleanType.TRUE))
                }
                ?: unexpectedType(value, "${generalizedDescriptionOf<StringType>()} or a value that has Symbol.replace method")
        )
    }
    val old = oldArg.value
    checkNewArg(new)
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
