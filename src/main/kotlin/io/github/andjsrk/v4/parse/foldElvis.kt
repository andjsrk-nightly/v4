package io.github.andjsrk.v4.parse

internal fun <T, R> Iterable<T>.foldElvis(operation: (T) -> R?) =
    fold(null as R?) { acc, it -> acc ?: operation(it) }
