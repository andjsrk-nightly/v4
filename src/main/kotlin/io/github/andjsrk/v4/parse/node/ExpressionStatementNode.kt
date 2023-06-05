package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.getValueOrReturn
import io.github.andjsrk.v4.evaluate.returnIfAbrupt
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ExpressionStatementNode(
    val expression: ExpressionNode,
    semicolonRange: Range?,
): StatementNode, NonAtomicNode {
    override val childNodes get() = listOf(expression)
    override val range = expression.range.extendCarefully(semicolonRange)
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
    override fun evaluate(): Completion {
        val exprRef = returnIfAbrupt(expression.evaluate()) { return it }
        val res = getValueOrReturn(exprRef) { return it }
        return Completion.normal(res)
    }
}
