package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBeString
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod

private val REPLACEMENT_CHARACTER = '\uFFFD'

val toWellFormed = builtinMethod("toWellFormed") fn@ { thisArg, args ->
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
