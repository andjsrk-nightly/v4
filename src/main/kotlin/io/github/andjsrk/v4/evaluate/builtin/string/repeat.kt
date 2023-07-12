package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

val repeat = builtinMethod("repeat", 1u) fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    val count = args[0]
        .requireToBe<NumberType> { return@fn it }
        .requireToBeUnsignedInt { return@fn it }
    Completion.Normal(
        if (count == 0) StringType.empty
        else string.repeat(count).languageValue
    )
}
