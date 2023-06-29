package io.github.andjsrk.v4.evaluate.builtin.number

import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val toRadix = BuiltinFunctionType("toRadix", 1u) fn@ { thisArg, args ->
    val number = thisArg.requireToBe<NumberType> { return@fn it }
    val radix = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeValidRadix { return@fn it }
    Completion.Normal(
        number.toString(radix.value.toInt())
    )
}
