package io.github.andjsrk.v4.parse.node

sealed interface ClassNode: Node {
    val name: IdentifierNode?
    val elements: List<ClassElementNode>
}
