package io.github.andjsrk.v4

internal inline fun Boolean.thenAlso(block: () -> Unit) =
    also {
        if (this) block()
    }
