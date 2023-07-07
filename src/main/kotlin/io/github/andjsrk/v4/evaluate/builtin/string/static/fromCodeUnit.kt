package io.github.andjsrk.v4.evaluate.builtin.string.static

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val fromCodeUnit = BuiltinFunctionType("fromCodeUnit") fn@ { _, args ->
    val builder = StringBuilder(args.size)
    for (arg in args) {
        val codeUnit = arg
            .requireToBe<NumberType> { return@fn it }
            .requireToBeIntWithin(Ranges.uint16, "A code unit") { return@fn it }
            .value
            .toInt()
        builder.append(codeUnit.toChar())
    }
    Completion.Normal(
        builder.toString().languageValue
    )
}
