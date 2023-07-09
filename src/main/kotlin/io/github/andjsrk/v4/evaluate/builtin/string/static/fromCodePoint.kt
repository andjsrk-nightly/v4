package io.github.andjsrk.v4.evaluate.builtin.string.static

import io.github.andjsrk.v4.MAX_CODE_POINT
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val fromCodePoint = BuiltinFunctionType("fromCodePoint") fn@ { _, args ->
    val builder = StringBuilder()
    for (arg in args) {
        val codePoint = arg
            .requireToBe<NumberType> { return@fn it }
            .requireToBeIntWithin(0L..MAX_CODE_POINT, "The code point") { return@fn it }
            .toInt()
        builder.appendCodePoint(codePoint)
    }
    Completion.Normal(
        builder.toString().languageValue
    )
}
