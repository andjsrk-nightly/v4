package io.github.andjsrk.v4

class Stack<E>(vararg elements: E) {
    private val queue = ArrayDeque(elements.asList())
    val top get() =
        queue.last()
    fun push(element: E) {
        queue.addLast(element)
    }
    fun pop() =
        queue.removeLast()
}
