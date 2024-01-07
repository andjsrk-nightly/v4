package io.github.andjsrk.v4.parse

internal fun <T> Sequence<T?>.foldElvis() =
    foldNull<_, T> { acc, it -> acc ?: it }
