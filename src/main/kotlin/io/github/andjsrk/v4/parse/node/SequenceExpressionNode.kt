package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class SequenceExpressionNode(
    val expressions: List<ExpressionNode>,
    override val range: Range,
): ExpressionNode {
    override fun toString() =
        stringifyLikeDataClass(::expressions, ::range)
}
