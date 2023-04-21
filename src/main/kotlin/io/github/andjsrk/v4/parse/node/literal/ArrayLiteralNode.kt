package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.node.ExpressionNode
import io.github.andjsrk.v4.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

private typealias ArrayItems = List<ExpressionNode>

class ArrayLiteralNode(
    override val value: ArrayItems,
    override val range: Range,
): DynamicLiteralNode<ArrayItems> {
    override fun toString() =
        stringifyLikeDataClass(::value, ::range)
    class Unsealed: DynamicLiteralNode.Unsealed<ArrayItems> {
        lateinit var startToken: Token
        lateinit var endToken: Token
        val value = mutableListOf<ExpressionNode>()
        override fun toSealed() =
            ArrayLiteralNode(
                value.toList(),
                startToken.range until endToken.range,
            )
    }
}
