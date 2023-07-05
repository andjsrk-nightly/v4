package io.github.andjsrk.v4

class Stack<E>(vararg elements: E) {
    private val queue = ArrayDeque(elements.asList())
    val top get() =
        queue.first()
    fun push(element: E) {
        queue.addFirst(element)
    }
    fun pop() =
        queue.removeFirst()
    fun toList() =
        queue.toList()
}
