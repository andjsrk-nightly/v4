package io.github.andjsrk.v4.evaluate.builtin.string.static

import io.github.andjsrk.v4.evaluate.Ranges
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.NumberType
import io.github.andjsrk.v4.evaluate.type.lang.requireToBeIntegerWithin

val fromCodeUnit = BuiltinFunctionType("fromCodeUnit") fn@ { _, args ->
    val builder = StringBuilder(args.size)
    for (arg in args) {
        val codeUnit = arg
            .requireToBe<NumberType> { return@fn it }
            .requireToBeIntegerWithin(Ranges.uint16, "A code unit") { return@fn it }
        builder.append(codeUnit.toChar())
    }
    Completion.Normal(
        builder.toString().languageValue
    )
}
