package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.Ranges
import io.github.andjsrk.v4.evaluate.getOptional
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod
import io.github.andjsrk.v4.evaluate.type.lang.requireToBeIntegerWithin

val padEnd = builtinMethod("padEnd", 1u) fn@ { thisArg, args ->
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
