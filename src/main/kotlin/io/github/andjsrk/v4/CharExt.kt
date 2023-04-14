package io.github.andjsrk.v4

internal fun Char.toHexIntOrNull() =
    digitToIntOrNull(16)
internal fun String.toHexIntOrNull() =
    toIntOrNull(16)
