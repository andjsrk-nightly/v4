package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class WhileNode(
    val test: ExpressionNode,
    override val body: StatementNode,
    val atLeastOnce: Boolean,
    startRange: Range,
): IterationStatementNode {
    override val childNodes get() = listOf(test, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::test, ::body, ::atLeastOnce, ::range)
}
