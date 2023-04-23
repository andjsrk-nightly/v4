package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ExpressionStatementNode(val expression: ExpressionNode): StatementNode {
    override val range = expression.range
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
}
