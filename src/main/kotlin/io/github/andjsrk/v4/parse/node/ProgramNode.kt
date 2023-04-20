package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.stringifyLikeDataClass

class ProgramNode(statements: List<StatementNode>): BlockStatementNode(statements) {
    override fun toString() =
        stringifyLikeDataClass(::statements)
    class Unsealed: BlockStatementNode.Unsealed() {
        override fun toSealed() =
            ProgramNode(statements.toList())
    }
}
