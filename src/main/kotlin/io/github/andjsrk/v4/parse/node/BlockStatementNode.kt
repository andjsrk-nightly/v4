package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class BlockStatementNode(
    override val statements: List<StatementNode>,
    override val range: Range,
): StatementNode, BlockNode {
    override val childNodes = statements
    override fun toString() =
        stringifyLikeDataClass(::statements, ::range)
}
