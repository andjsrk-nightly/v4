package io.github.andjsrk.v4.evaluate.builtin.string.static

import io.github.andjsrk.v4.evaluate.builtin.string.requireToBeCodePoint
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.NumberType

val fromCodePoint = BuiltinFunctionType("fromCodePoint") fn@ { _, args ->
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
