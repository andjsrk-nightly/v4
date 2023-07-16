package io.github.andjsrk.v4.evaluate.builtin.string

import io.github.andjsrk.v4.not

private typealias CodePoint = Int

internal fun CodePoint.isUnpairedSurrogate(): Boolean {
    val chars = this.toChars()
    val first = chars[0]
    if (first.not { isSurrogate() }) return false
    if (first.isLowSurrogate()) return true
    val second = chars.getOrNull(1) ?: return true
    return second.not { isLowSurrogate() }
}

private fun CodePoint.toChars() =
    Character.toChars(this)
