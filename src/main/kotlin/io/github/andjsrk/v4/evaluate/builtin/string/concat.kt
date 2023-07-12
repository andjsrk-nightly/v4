package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod

val concat = builtinMethod("concat") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val builder = StringBuilder(string)
    for (arg in args) {
        val str = arg.requireToBeString { return@fn it }
        builder.append(str)
    }
    Completion.Normal(
        builder.toString().languageValue
    )
}
