package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class BlockStatementNode(
    override val statements: List<StatementNode>,
    override val range: Range,
): BlockNode, StatementNode {
    override fun toString() =
        stringifyLikeDataClass(::statements, ::range)
    class Unsealed: ComplexNode.Unsealed {
        lateinit var startRange: Range
        lateinit var endRange: Range
        val statements = mutableListOf<StatementNode>()
        override fun toSealed() =
            BlockStatementNode(
                statements.toList(),
                startRange until endRange,
            )
    }
}
