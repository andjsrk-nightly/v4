package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class IfNode(
    override val test: ExpressionNode,
    override val body: StatementNode,
    startRange: Range,
): ConditionalStatementNode {
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::test, ::body, ::range)
}
