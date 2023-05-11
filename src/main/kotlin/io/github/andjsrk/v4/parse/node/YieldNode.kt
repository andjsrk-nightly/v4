package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

open class YieldNode(
    open val expression: ExpressionNode?,
    override val range: Range,
): ExpressionNode, NonAtomicNode {
    override val childNodes by lazy { listOf(expression) }
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
}
