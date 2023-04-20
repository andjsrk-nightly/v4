package io.github.andjsrk.v4

internal inline val <reified T: Any> T.className get() =
    this::class.simpleName!!
