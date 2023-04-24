package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

abstract class FixedCalleeCallExpressionNode(
    val arguments: Arguments,
    range: Range,
): CallSyntaxExpressionNode(range) {
    override fun toString() =
        stringifyLikeDataClass(::arguments, ::range)
    abstract class Unsealed: CallSyntaxExpressionNode.Unsealed() {
        var arguments = mutableListOf<CommaSeparatedElementNode<ExpressionNode>>()
        abstract override fun toSealed(): FixedCalleeCallExpressionNode
    }
}
