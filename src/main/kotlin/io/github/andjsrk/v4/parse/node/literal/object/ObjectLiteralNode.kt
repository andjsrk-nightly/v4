package io.github.andjsrk.v4.parse.node.literal.`object`

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.node.literal.LiteralNode
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ObjectLiteralNode(
    val elements: List<ObjectElementNode>,
    override val range: Range,
): LiteralNode {
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
}
