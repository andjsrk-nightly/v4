package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod

val localeCompare = builtinMethod("localeCompare", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val that = args[0].requireToBeString { return@fn it }
    TODO()
}
