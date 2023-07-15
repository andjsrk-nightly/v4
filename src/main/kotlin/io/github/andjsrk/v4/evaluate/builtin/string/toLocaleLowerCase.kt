package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.requireToBeString
import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod

val toLocaleLowerCase = builtinMethod("toLocaleLowerCase") fn@ { thisArg, _ ->
    val string = thisArg.requireToBeString { return@fn it }
    TODO()
}
