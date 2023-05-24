package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class SuperPropertyNode(
    override val `object`: SuperNode,
    override val property: ExpressionNode,
    override val isComputed: Boolean,
    endRange: Range,
): MemberExpressionLikeNode {
    override val childNodes = listOf(`object`, property)
    override val range = `object`.range..endRange
    override fun toString() =
        stringifyLikeDataClass(::`object`, ::property, ::isComputed, ::range)
}
