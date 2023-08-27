package io.github.andjsrk.v4

class Stack<out E>(vararg elements: E) {
    private val queue = ArrayDeque(elements.asList())
    val top get() =
        queue.first()
    fun addTop(element: @UnsafeVariance E) {
        queue.addFirst(element)
    }
    fun removeTop() =
        queue.removeFirst()
    operator fun contains(value: @UnsafeVariance E) =
        value in queue
    fun toList() =
        queue.toList()
}
