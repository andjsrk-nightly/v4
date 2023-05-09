package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

internal typealias ExpressionOrBindingPatternNode = Node

internal class CoverParenthesizedExpressionAndArrowParameterListNode(
    val elements: List<ExpressionOrBindingPatternNode>,
    override val range: Range,
): ExpressionNode/* for compatibility */ {
    override fun toString() =
        stringifyLikeDataClass(::elements)
}
