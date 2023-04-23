package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

abstract class FixedCalleeCallExpressionNode(
    val arguments: List<CommaSeparatedElementNode>,
    range: Range,
): CallSyntaxExpressionNode(range) {
    override fun toString() =
        stringifyLikeDataClass(::arguments, ::range)
    abstract class Unsealed: CallSyntaxExpressionNode.Unsealed() {
        var arguments = mutableListOf<CommaSeparatedElementNode>()
        abstract override fun toSealed(): FixedCalleeCallExpressionNode
    }
}
