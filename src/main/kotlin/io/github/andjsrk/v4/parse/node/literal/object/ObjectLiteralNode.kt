package io.github.andjsrk.v4.parse.node.literal.`object`

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.node.ComplexNode
import io.github.andjsrk.v4.parse.node.literal.LiteralNode
import io.github.andjsrk.v4.parse.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

class ObjectLiteralNode(
    val elements: List<ObjectElementNode>,
    override val range: Range,
): LiteralNode, ComplexNode {
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    class Unsealed: ComplexNode.Unsealed {
        lateinit var startToken: Token
        lateinit var endToken: Token
        val elements = mutableListOf<ObjectElementNode>()
        override fun toSealed() =
            ObjectLiteralNode(
                elements.toList(),
                startToken.range until endToken.range,
            )
    }
}
