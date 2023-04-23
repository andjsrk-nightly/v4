package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class IfStatementNode(
    val test: ExpressionNode,
    val body: StatementNode,
    override val range: Range,
): StatementNode, ComplexNode {
    override fun toString() =
        stringifyLikeDataClass(::test, ::body, ::range)
    class Unsealed: ComplexNode.Unsealed {
        lateinit var startRange: Range
        lateinit var test: ExpressionNode
        lateinit var body: StatementNode
        override fun toSealed() =
            IfStatementNode(test, body, startRange until body.range)
    }
}
