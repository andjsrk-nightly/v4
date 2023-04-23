package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.node.ComplexNode
import io.github.andjsrk.v4.parse.node.ExpressionNode
import io.github.andjsrk.v4.parse.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

class ArrayLiteralNode(
    val items: List<ExpressionNode>,
    override val range: Range,
): LiteralNode, ComplexNode {
    override fun toString() =
        stringifyLikeDataClass(::items, ::range)
    class Unsealed: ComplexNode.Unsealed {
        lateinit var startToken: Token
        lateinit var endToken: Token
        val items = mutableListOf<ExpressionNode>()
        override fun toSealed() =
            ArrayLiteralNode(
                items.toList(),
                startToken.range until endToken.range,
            )
    }
}
