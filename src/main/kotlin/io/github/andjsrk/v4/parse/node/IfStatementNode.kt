package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.stringifyLikeDataClass

class IfStatementNode(
    val condition: ExpressionNode,
    val body: StatementNode,
): StatementNode {
    override fun toString() =
        stringifyLikeDataClass(::condition, ::body)
    class Unsealed: StatementNode.Unsealed {
        lateinit var condition: ExpressionNode
        lateinit var body: StatementNode
        override fun toSealed() =
            IfStatementNode(condition, body)
    }
}
