package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

sealed class FixedCalleeCallNode(
    val arguments: List<MaybeSpreadNode>,
    range: Range,
): CallSyntaxExpressionNode(range) {
    override fun toString() =
        stringifyLikeDataClass(::arguments, ::range)
    abstract class Unsealed: CallSyntaxExpressionNode.Unsealed() {
        var arguments = mutableListOf<MaybeSpreadNode>()
        abstract override fun toSealed(): FixedCalleeCallNode
    }
}
