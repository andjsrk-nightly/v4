package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NormalCallExpressionNode(
    callee: ExpressionNode,
    arguments: Arguments,
    val isOptionalChain: Boolean,
    range: Range,
): CallExpressionNode(callee, arguments, range) {
    override fun toString() =
        stringifyLikeDataClass(::callee, ::arguments, ::isOptionalChain, ::range)
    class Unsealed: CallExpressionNode.Unsealed() {
        var isOptionalChain = false
        override fun toSealed() =
            NormalCallExpressionNode(callee, arguments.toList(), isOptionalChain, callee.range..endRange)
    }
}
