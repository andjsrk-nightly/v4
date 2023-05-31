package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ExpressionStatementNode(
    val expression: ExpressionNode,
    semicolonRange: Range?,
): StatementNode, NonAtomicNode {
    override val childNodes get() = listOf(expression)
    override val range = expression.range.extendCarefully(semicolonRange)
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
}
