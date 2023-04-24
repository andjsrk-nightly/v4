package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ConditionalExpressionNode(
    val test: ExpressionNode,
    val consequent: ExpressionNode,
    val alternative: ExpressionNode,
): ExpressionNode {
    override val range = test.range until alternative.range
    override fun toString() =
        stringifyLikeDataClass(::test, ::consequent, ::alternative)
}
