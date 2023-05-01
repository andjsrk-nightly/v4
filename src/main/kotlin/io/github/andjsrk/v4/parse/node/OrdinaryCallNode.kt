package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class OrdinaryCallNode(
    callee: ExpressionNode,
    arguments: List<MaybeSpreadNode>,
    val isOptionalChain: Boolean,
    range: Range,
): CallNode(callee, arguments, range) {
    override fun toString() =
        stringifyLikeDataClass(::callee, ::arguments, ::isOptionalChain, ::range)
    class Unsealed: CallNode.Unsealed() {
        var isOptionalChain = false
        override fun toSealed() =
            OrdinaryCallNode(callee, arguments.toList(), isOptionalChain, callee.range..endRange)
    }
}
