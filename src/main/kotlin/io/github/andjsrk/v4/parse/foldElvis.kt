package io.github.andjsrk.v4.parse

internal fun <T> Iterable<T?>.foldElvis() =
    foldNull<_, T> { acc, it -> acc ?: it }

internal fun <T> Sequence<T?>.foldElvis() =
    foldNull<_, T> { acc, it -> acc ?: it }
