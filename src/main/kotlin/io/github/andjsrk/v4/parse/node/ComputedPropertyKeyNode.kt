package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ComputedPropertyKeyNode(
    val expression: ExpressionNode,
    override val range: Range,
): ObjectLiteralKeyNode, NonAtomicNode {
    override val childNodes = listOf(expression)
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
}
