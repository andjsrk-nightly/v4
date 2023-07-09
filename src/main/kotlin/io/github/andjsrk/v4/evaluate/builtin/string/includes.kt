package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.getOptional
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType

val includes = BuiltinFunctionType("includes", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val search = args[0].requireToBeString { return@fn it }
    val startIndex = args.getOptional(1)
        ?.requireToBeStartIndex(string) { return@fn it }
        ?: 0
    Completion.Normal(
        BooleanType.from(
            string.indexOf(search, startIndex) != -1
        )
    )
}
