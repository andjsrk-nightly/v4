package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*

val endsWith = BuiltinFunctionType("endsWith", 1u) fn@ { thisArg, args ->
    val string = thisArg
        .requireToBe<StringType> { return@fn it }
        .value
    val search = args[0]
        .requireToBe<StringType> { return@fn it }
        .value
    val endPosition = args.getOptional(1)
        .requireToBeNullable<NumberType> { return@fn it }
        ?.requireToBeIndex { return@fn it }
        ?.value
        ?.toInt()
        ?: string.length
    if (endPosition > string.length) TODO()
    TODO()
}
