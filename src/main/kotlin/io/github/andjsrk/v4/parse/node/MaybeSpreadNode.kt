package io.github.andjsrk.v4.parse.node

sealed interface MaybeSpreadNode: Node {
    val expression: ExpressionNode
}
