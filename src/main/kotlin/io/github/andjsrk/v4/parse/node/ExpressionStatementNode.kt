package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.stringifyLikeDataClass

class ExpressionStatementNode(val expression: ExpressionNode): StatementNode {
    override fun toString() =
        stringifyLikeDataClass(::expression)
    class Unsealed: StatementNode.Unsealed {
        lateinit var expression: ExpressionNode
        override fun toSealed() =
            ExpressionStatementNode(expression)
    }
}
