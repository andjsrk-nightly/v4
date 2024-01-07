package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class SpreadNode(
    override val expression: ExpressionNode,
    startRange: Range,
): MaybeSpreadNode {
    override val childNodes get() = listOf(expression)
    override val range = startRange..expression.range
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
}
