package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class IfNode(
    val test: ExpressionNode,
    override val body: StatementNode,
    val elseBody: StatementNode?,
    startRange: Range,
): StatementNode, HasStatementBody {
    override val childNodes = listOf(test, body, elseBody)
    override val range = startRange..(elseBody ?: body).range
    override fun toString() =
        stringifyLikeDataClass(::test, ::body, ::elseBody, ::range)
}
