package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class DefaultExportDeclarationNode(
    val expression: ExpressionNode,
    startRange: Range,
    semicolonRange: Range?,
): ExportDeclarationNode {
    override val childNodes get() = listOf(expression)
    override val range = startRange..(semicolonRange ?: expression.range)
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
}
