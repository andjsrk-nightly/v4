package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.stringifyLikeDataClass

class ProgramNode(statements: List<StatementNode>):
    BlockStatementNode(
        statements,
        (
            if (statements.isEmpty()) Range(0, 0)
            else statements.first().range until statements.last().range
        ),
    )
{
    override fun toString() =
        stringifyLikeDataClass(::statements, ::range)
    class Unsealed: BlockStatementNode.Unsealed() {
        override fun toSealed() =
            ProgramNode(statements.toList())
    }
}
