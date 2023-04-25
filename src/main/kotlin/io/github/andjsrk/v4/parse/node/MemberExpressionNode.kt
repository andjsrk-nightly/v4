package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class MemberExpressionNode(
    val `object`: ExpressionNode,
    val property: ExpressionNode,
    val isOptionalChain: Boolean,
    val isComputed: Boolean,
    override val range: Range,
): ExpressionNode, ComplexNode {
    override fun toString() =
        stringifyLikeDataClass(::`object`, ::property, ::isComputed, ::isOptionalChain, ::range)
    class Unsealed: ComplexNode.Unsealed {
        lateinit var endRange: Range
        lateinit var `object`: ExpressionNode
        lateinit var property: ExpressionNode
        var isOptionalChain = false
        var isComputed = false
        override fun toSealed() =
            MemberExpressionNode(
                `object`,
                property,
                isOptionalChain,
                isComputed,
                `object`.range..endRange,
            )
    }
}
