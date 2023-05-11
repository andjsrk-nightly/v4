package io.github.andjsrk.v4.parse.node

sealed interface ClassNode: NonAtomicNode {
    val parent: ExpressionNode?
    val elements: List<ClassElementNode>
}
