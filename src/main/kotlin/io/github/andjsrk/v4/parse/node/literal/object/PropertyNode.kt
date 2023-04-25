package io.github.andjsrk.v4.parse.node.literal.`object`

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.node.ComplexNode
import io.github.andjsrk.v4.parse.node.ExpressionNode
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

open class PropertyNode(
    val key: ExpressionNode,
    val value: ExpressionNode,
    val isComputed: Boolean,
    override val range: Range,
): ObjectElementNode, ComplexNode {
    override fun toString() =
        stringifyLikeDataClass(::key, ::value, ::isComputed, ::range)
    class Unsealed: ComplexNode.Unsealed {
        lateinit var startRange: Range
        lateinit var key: ExpressionNode
        lateinit var value: ExpressionNode
        var isComputed = false
        override fun toSealed() =
            PropertyNode(key, value, isComputed, startRange..value.range)
    }
}
