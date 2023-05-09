package io.github.andjsrk.v4.parse.node

sealed interface MaybeSpreadNode: Node, ObjectElementNode {
    val expression: ExpressionNode
}
