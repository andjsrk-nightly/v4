package io.github.andjsrk.v4

fun <T, R> Iterable<T>.mapAsSequence(transform: (T) -> R) =
    this.asSequence().map(transform)
