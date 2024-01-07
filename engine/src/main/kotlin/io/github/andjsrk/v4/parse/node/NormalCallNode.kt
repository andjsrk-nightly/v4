package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NormalCallNode(
    callee: ExpressionNode,
    arguments: ArgumentsNode,
    val isOptionalChain: Boolean,
): CallNode(callee, arguments) {
    override fun toString() =
        stringifyLikeDataClass(::callee, ::arguments, ::isOptionalChain, ::range)
}
