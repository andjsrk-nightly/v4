package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

open class CallExpressionNode(
    val callee: ExpressionNode,
    arguments: List<CommaSeparatedElementNode>,
    range: Range,
): FixedCalleeCallExpressionNode(arguments, range) {
    override fun toString() =
        stringifyLikeDataClass(::callee, ::arguments, ::range)
    open class Unsealed: FixedCalleeCallExpressionNode.Unsealed() {
        lateinit var callee: ExpressionNode
        override fun toSealed() =
            CallExpressionNode(callee, arguments.toList(), startRange until endRange)
    }
}
