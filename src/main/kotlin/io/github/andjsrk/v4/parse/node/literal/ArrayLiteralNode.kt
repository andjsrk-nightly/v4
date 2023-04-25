package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.node.CommaSeparatedElementNode
import io.github.andjsrk.v4.parse.node.ExpressionNode
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ArrayLiteralNode(
    val items: List<CommaSeparatedElementNode>,
    override val range: Range,
): LiteralNode {
    override fun toString() =
        stringifyLikeDataClass(::items, ::range)
}
