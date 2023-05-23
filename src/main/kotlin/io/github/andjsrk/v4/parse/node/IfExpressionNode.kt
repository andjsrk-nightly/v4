package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

class IfExpressionNode(
    test: ExpressionNode,
    then: ExpressionNode,
    override val `else`: ExpressionNode,
    startRange: Range
): IfNode<ExpressionNode>(test, then, `else`), ExpressionNode {
    override val range = startRange..`else`.range
}
