package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.stringifyLikeDataClass

open class BlockStatementNode(val statements: List<StatementNode>): StatementNode {
    override fun toString() =
        stringifyLikeDataClass(::statements)
    open class Unsealed: StatementNode.Unsealed {
        val statements = mutableListOf<StatementNode>()
        override fun toSealed() =
            BlockStatementNode(statements.toList())
    }
}
