package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.StringType

val concat = BuiltinFunctionType("concat") fn@ { thisArg, args ->
    val string = thisArg
        .requireToBe<StringType> { return@fn it }
        .value
    val builder = StringBuilder(string)
    for (arg in args) {
        val str = arg
            .requireToBe<StringType> { return@fn it }
            .value
        builder.append(str)
    }
    Completion.Normal(
        builder.toString().languageValue
    )
}
