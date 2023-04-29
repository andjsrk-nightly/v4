package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class SpreadNode(
    override val expression: ExpressionNode,
    override val range: Range,
): MaybeSpreadNode {
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
}
