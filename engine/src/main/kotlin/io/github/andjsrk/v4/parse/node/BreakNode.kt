package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class BreakNode(override val range: Range): ExpressionNode {
    override fun toString() =
        stringifyLikeDataClass(::range)
    override fun evaluate() =
        Completion.Break(null, null)
}
