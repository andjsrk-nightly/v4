package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

sealed class FixedArgumentCallNode(
    open val callee: ExpressionNode,
): ExpressionNode, NonAtomicNode {
    override val childNodes get() = listOf<Node?>(callee)
    override fun toString() =
        stringifyLikeDataClass(::callee, ::range)
}
