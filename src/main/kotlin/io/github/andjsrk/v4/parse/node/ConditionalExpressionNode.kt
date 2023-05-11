package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ConditionalExpressionNode(
    val test: ExpressionNode,
    val consequent: ExpressionNode,
    val alternative: ExpressionNode,
): ExpressionNode, NonAtomicNode {
    override val childNodes = listOf(test, consequent, alternative)
    override val range = test.range..alternative.range
    override fun toString() =
        stringifyLikeDataClass(::test, ::consequent, ::alternative)
}
