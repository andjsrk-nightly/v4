package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

open class NonSpreadNode(override val expression: ExpressionNode): MaybeSpreadNode {
    override val childNodes get() = listOf(expression)
    override val range by lazy { expression.range }
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
}
