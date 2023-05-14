package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ExpressionStatementNode(
    val expression: ExpressionNode,
    semicolonRange: Range?,
): StatementNode, NonAtomicNode {
    override val childNodes = listOf(expression)
    override val range = expression.range..(semicolonRange ?: expression.range)
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
}
