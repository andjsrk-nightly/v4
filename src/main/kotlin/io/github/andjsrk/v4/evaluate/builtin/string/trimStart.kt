package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBeString
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod

val trimStart = builtinMethod("trimStart") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    Completion.Normal(
        string.trimStart().languageValue
    )
}
