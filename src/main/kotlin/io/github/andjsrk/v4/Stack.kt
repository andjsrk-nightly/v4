package io.github.andjsrk.v4

class Stack<out E>(vararg elements: E): Collection<E> {
    private val queue = ArrayDeque(elements.asList())
    override val size
        get() = queue.size
    override fun isEmpty() =
        queue.isEmpty()
    val top get() =
        queue.first()
    fun addTop(element: @UnsafeVariance E) {
        queue.addFirst(element)
    }
    fun removeTop() =
        queue.removeFirst()
    override operator fun contains(element: @UnsafeVariance E) =
        element in queue
    fun toList() =
        queue.toList()

    override fun iterator(): Iterator<E> =
        queue.iterator()
    override fun containsAll(elements: Collection<@UnsafeVariance E>) =
        queue.containsAll(elements)
}
