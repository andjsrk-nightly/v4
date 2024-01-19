package io.github.andjsrk.v4

interface IterableIterator<out T>: Iterable<T>, Iterator<T> {
    override fun iterator() = this
}

fun <T> Iterator<T>.toIterableIterator(): IterableIterator<T> =
    object: IterableIterator<T>, Iterator<T> by this {}
