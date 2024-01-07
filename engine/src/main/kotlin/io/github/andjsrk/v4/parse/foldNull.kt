package io.github.andjsrk.v4.parse

internal inline fun <T, R> Sequence<T>.foldNull(operation: (R?, T) -> R?) =
    fold(null as R?, operation)
