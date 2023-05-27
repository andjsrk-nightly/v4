package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class MemberExpressionNode(
    override val `object`: ExpressionNode,
    override val property: ExpressionNode,
    val isOptionalChain: Boolean,
    override val isComputed: Boolean,
    endRange: Range,
): MemberExpressionLikeNode {
    override val childNodes get() = listOf(`object`, property)
    override val range = `object`.range..endRange
    override fun toString() =
        stringifyLikeDataClass(::`object`, ::property, ::isOptionalChain, ::isComputed, ::range)
}
