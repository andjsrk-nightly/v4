package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val padStart = builtinMethod("padStart", 1u) fn@ { thisArg, args ->
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
