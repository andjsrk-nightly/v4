package io.github.andjsrk.v4

fun Char.toHexIntOrNull() =
    digitToIntOrNull(16)
fun String.toHexIntOrNull() =
    toIntOrNull(16)
