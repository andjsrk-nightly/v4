package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

sealed class CallNode(
    callee: ExpressionNode,
    val arguments: ArgumentsNode,
): FixedArgumentCallNode(callee) {
    override val range = callee.range..arguments.range
    override val childNodes by lazy { super.childNodes + arguments }
    override fun toString() =
        stringifyLikeDataClass(::callee, ::arguments, ::range)
}
