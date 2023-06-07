package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ParenthesizedExpressionNode(
    val expression: ExpressionNode,
    override val range: Range,
): ExpressionNode, NonAtomicNode {
    override val childNodes get() = listOf(expression)
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
    override fun evaluate() =
        expression.evaluate()
}
