package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

class IfStatementNode(
    test: ExpressionNode,
    then: StatementNode,
    `else`: StatementNode?,
    startRange: Range,
): IfNode<StatementNode>(test, then, `else`), StatementNode {
    override val range = startRange..(`else` ?: then).range
}
