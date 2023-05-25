package io.github.andjsrk.v4

fun <T> T.isOneOf(vararg items: T) =
    this in items
