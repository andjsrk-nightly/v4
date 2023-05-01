package io.github.andjsrk.v4.parse.node

sealed interface ConditionalStatementNode: StatementNode {
    val test: ExpressionNode
    val body: StatementNode
}
