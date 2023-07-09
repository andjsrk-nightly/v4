package io.github.andjsrk.v4.evaluate.builtin.string

private typealias CodePoint = Int

internal fun CodePoint.toChars() =
    Character.toChars(this)
