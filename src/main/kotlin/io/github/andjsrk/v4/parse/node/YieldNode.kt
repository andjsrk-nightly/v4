package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class YieldNode(
    val expression: ExpressionNode?,
    val isDelegate: Boolean,
    override val range: Range,
): ExpressionNode {
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
}
