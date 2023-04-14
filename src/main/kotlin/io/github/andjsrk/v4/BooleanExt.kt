package io.github.andjsrk.v4

internal fun Boolean.thenAlso(block: () -> Unit) =
    also {
        if (this) block()
    }
