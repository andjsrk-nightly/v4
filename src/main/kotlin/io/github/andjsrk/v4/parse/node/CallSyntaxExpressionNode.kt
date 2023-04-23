package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

abstract class CallSyntaxExpressionNode(
    override val range: Range,
): ExpressionNode, ComplexNode {
    abstract class Unsealed: ComplexNode.Unsealed {
        lateinit var endRange: Range
    }
}
