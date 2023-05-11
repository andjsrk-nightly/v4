package io.github.andjsrk.v4.parse

infix fun <T> T.and(list: List<T>) =
    listOf(this) + list
