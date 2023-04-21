package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

open class BlockStatementNode(
    val statements: List<StatementNode>,
    override val range: Range,
): StatementNode {
    override fun toString() =
        stringifyLikeDataClass(::statements, ::range)
    open class Unsealed: StatementNode.Unsealed {
        lateinit var startToken: Token
        lateinit var endToken: Token
        val statements = mutableListOf<StatementNode>()
        override fun toSealed() =
            BlockStatementNode(
                statements.toList(),
                startToken.range until endToken.range,
            )
    }
}
