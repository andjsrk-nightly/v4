package io.github.andjsrk.v4.parse

internal fun <T> List<T>?.orEmpty() =
    this ?: emptyList()
