package io.github.andjsrk.v4.evaluate

fun <T> Iterator<T>.withoutLast() =
    iterator {
        while (hasNext()) {
            val value = next()
            // last element has no next element
            if (hasNext()) yield(value)
        }
    }
