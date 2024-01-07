package io.github.andjsrk.v4.parse.node

/**
 * Represents a [Node] that contains other [Node]s.
 */
sealed interface NonAtomicNode: Node {
    val childNodes: List<Node?>
}
