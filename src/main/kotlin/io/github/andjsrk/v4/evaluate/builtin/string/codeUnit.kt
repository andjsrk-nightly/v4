package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val codeUnit = BuiltinFunctionType("codeUnit") fn@ { thisArg, args ->
    val string = thisArg
        .requireToBe<StringType> { return@fn it }
        .value
    val index = args.getOptional(0)
        .requireToBeNullable<NumberType> { return@fn it }
        ?.requireToBeRelativeIndex(string.length) { return@fn it }
        ?.run {
            resolveRelativeIndex(string.length) ?: return@fn Completion.Normal.`null`
        }
        ?: 0
    if (string.isEmpty()) return@fn Completion.Normal.`null`
    assert(index <= string.length)
    Completion.Normal(
        string[index].code.languageValue
    )
}
