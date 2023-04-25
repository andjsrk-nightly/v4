package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.node.literal.`object`.ObjectElementNode
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class CommaSeparatedElementNode(
    val expression: ExpressionNode,
    val isSpread: Boolean,
    override val range: Range,
): ObjectElementNode {
    override fun toString() =
        stringifyLikeDataClass(::expression, ::isSpread, ::range)
}
