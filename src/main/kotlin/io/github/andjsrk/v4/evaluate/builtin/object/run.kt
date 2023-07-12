package io.github.andjsrk.v4.evaluate.builtin.`object`

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.lang.FunctionType
import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod

val run = builtinMethod("run".languageValue, 1u) fn@ { thisArg, args ->
    val func = args[0].requireToBe<FunctionType> { return@fn it }
    func._call(thisArg, listOf(thisArg))
}
