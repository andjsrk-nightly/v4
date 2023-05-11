package io.github.andjsrk.v4.parse.node

sealed interface ConditionalStatementNode: StatementNode, NonAtomicNode {
    val test: ExpressionNode
    val body: StatementNode
}
