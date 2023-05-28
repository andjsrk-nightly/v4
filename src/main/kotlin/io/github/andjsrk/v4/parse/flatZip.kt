package io.github.andjsrk.v4.parse

internal fun <T, C: Iterable<T>> C.flatZip(other: C) =
    sequence {
        val firstIt = iterator()
        val secondIt = other.iterator()
        while (firstIt.hasNext() && secondIt.hasNext()) {
            yield(firstIt.next())
            yield(secondIt.next())
        }
        yieldAll(firstIt)
        yieldAll(secondIt)
    }
        .toList()
