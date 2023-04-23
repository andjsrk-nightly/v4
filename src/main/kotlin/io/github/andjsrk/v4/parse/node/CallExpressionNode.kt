package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

abstract class CallExpressionNode(
    val callee: ExpressionNode,
    arguments: List<CommaSeparatedElementNode>,
    range: Range,
): FixedCalleeCallExpressionNode(arguments, range) {
    override fun toString() =
        stringifyLikeDataClass(::callee, ::arguments, ::range)
    abstract class Unsealed: FixedCalleeCallExpressionNode.Unsealed() {
        lateinit var callee: ExpressionNode
    }
}
