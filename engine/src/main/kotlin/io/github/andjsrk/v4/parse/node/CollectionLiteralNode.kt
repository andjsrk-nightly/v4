package io.github.andjsrk.v4.parse.node

sealed interface CollectionLiteralNode<E: Node>: LiteralNode, NonAtomicNode {
    val elements: List<E>
}
