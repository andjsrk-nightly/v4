package io.github.andjsrk.v4

internal inline val Any.className get() =
    this::class.simpleName!!
