package io.github.andjsrk.v4

internal inline fun neverHappens(): Nothing =
    throw Error("This can never happen")
