package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class YieldNode(
    val expression: ExpressionNode,
    val isSpread: Boolean,
    startRange: Range,
): ExpressionNode, NonAtomicNode {
    override val range = startRange..expression.range
    override val childNodes = listOf(expression)
    override fun toString() =
        stringifyLikeDataClass(::expression, ::isSpread, ::range)
}
