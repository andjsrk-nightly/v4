package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.node.ExpressionNode
import io.github.andjsrk.v4.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

private typealias ObjectProperties = Map<ExpressionNode, ExpressionNode>

class ObjectLiteralNode(
    override val value: ObjectProperties,
    override val range: Range,
): DynamicLiteralNode<ObjectProperties> {
    override fun toString() =
        stringifyLikeDataClass(::value, ::range)
    class Unsealed: DynamicLiteralNode.Unsealed<ObjectProperties> {
        lateinit var startToken: Token
        lateinit var endToken: Token
        val value = mutableMapOf<ExpressionNode, ExpressionNode>()
        override fun toSealed() =
            ObjectLiteralNode(
                value.toMap(),
                startToken.range until endToken.range,
            )
    }
}
