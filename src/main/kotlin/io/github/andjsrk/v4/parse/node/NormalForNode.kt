package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NormalForNode(
    override val test: ExpressionNode,
    override val body: StatementNode,
    override val range: Range,
): ForNode, ConditionalStatementNode {
    override val childNodes = listOf(test, body)
    override fun toString() =
        stringifyLikeDataClass(::test, ::body, ::range)
}
