package io.github.andjsrk.v4.parse

/**
 * Returns a list that has alternately element of this collection and element of the other array with the same index.
 * The returned list has length as long as possible.
 *
 * @see Iterable.zip
 */
internal fun <T> Iterable<T>.flatZip(other: Iterable<T>) =
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
