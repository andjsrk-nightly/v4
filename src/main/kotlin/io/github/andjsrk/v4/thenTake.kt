package io.github.andjsrk.v4

internal inline fun <R> Boolean.thenTake(block: () -> R) =
    if (this) block()
    else null
