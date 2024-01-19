package io.github.andjsrk.v4.evaluate

fun <T, R> Iterator<T>.map(transform: (T) -> R) =
    iterator {
        forEach { yield(transform(it)) }
    }
