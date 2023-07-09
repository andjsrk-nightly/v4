package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType
import io.github.andjsrk.v4.evaluate.type.lang.BuiltinFunctionType
import io.github.andjsrk.v4.not

val isWellFormed = BuiltinFunctionType("isWellFormed") fn@ { thisArg, args ->
    val string = thisArg.requireToBeString { return@fn it }
    var index = 0
    while (index < string.length) {
        val chars = string.codePointAt(index).toChars()
        if (chars.isUnpairedSurrogate()) return@fn Completion.Normal(BooleanType.FALSE)
        index += chars.size
    }
    Completion.Normal(BooleanType.TRUE)
}

private fun CharArray.isUnpairedSurrogate(): Boolean {
    val first = this[0]
    if (first.not { isSurrogate() }) return false
    if (first.isLowSurrogate()) return true
    val second = getOrNull(1) ?: return true
    return second.not { isLowSurrogate() }
}
