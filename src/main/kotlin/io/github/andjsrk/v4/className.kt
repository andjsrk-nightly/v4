package io.github.andjsrk.v4

internal inline val <T: Any> T.className get() =
    this::class.simpleName!!
