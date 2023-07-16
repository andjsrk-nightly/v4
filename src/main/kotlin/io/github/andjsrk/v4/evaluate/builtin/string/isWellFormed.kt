package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.requireToBeString
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType
import io.github.andjsrk.v4.evaluate.type.lang.builtinMethod

val isWellFormed = builtinMethod("isWellFormed") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    for (codePoint in string.codePoints()) {
        if (codePoint.isUnpairedSurrogate()) return@fn Completion.Normal(BooleanType.FALSE)
    }
    Completion.Normal(BooleanType.TRUE)
}
