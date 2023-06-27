package io.github.andjsrk.v4.evaluate.builtin.`object`

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.FunctionType

val run = BuiltinFunctionType("run".languageValue, 1u) { thisArg, args ->
    val func = args[0]
    func.requireToBe<FunctionType> { return@BuiltinFunctionType it }
    func._call(thisArg, listOf(thisArg))
}
