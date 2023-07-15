package io.github.andjsrk.v4.evaluate.builtin.string

internal fun Int.coerceInString(string: String) =
    coerceIn(0, string.length)
