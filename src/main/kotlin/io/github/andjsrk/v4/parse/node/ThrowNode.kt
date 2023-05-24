package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ThrowNode(
    val expression: ExpressionNode,
    startRange: Range,
    semicolonRange: Range?,
): StatementNode, NonAtomicNode {
    override val childNodes = listOf(expression)
    override val range = startRange..(semicolonRange ?: expression.range)
    override fun toString() =
        stringifyLikeDataClass(::expression)
}