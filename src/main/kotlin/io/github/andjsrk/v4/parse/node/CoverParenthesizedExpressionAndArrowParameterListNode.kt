package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

internal typealias ExpressionOrBindingElementNode = Node

internal class CoverParenthesizedExpressionAndArrowParameterListNode(
    val items: List<ExpressionOrBindingElementNode>,
    override val range: Range,
): ExpressionNode/* for compatibility */ {
    override fun toString() =
        stringifyLikeDataClass(::items)
}
