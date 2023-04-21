package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

class IfStatementNode(
    val condition: ExpressionNode,
    val body: StatementNode,
    override val range: Range,
): StatementNode {
    override fun toString() =
        stringifyLikeDataClass(::condition, ::body, ::range)
    class Unsealed: StatementNode.Unsealed {
        lateinit var startToken: Token
        lateinit var endToken: Token
        lateinit var condition: ExpressionNode
        lateinit var body: StatementNode
        override fun toSealed() =
            IfStatementNode(
                condition,
                body,
                startToken.range until endToken.range,
            )
    }
}
