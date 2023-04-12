package io.github.andjsrk.v4.util

fun <T> T.isOneOf(vararg items: T) =
    this in items
