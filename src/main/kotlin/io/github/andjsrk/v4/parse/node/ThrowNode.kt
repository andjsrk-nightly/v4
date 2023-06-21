package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.evaluateValueOrReturn
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ThrowNode(
    val expression: ExpressionNode,
    startRange: Range,
    semicolonRange: Range?,
): StatementNode, NonAtomicNode {
    override val childNodes get() = listOf(expression)
    override val range = startRange..expression.range.extendCarefully(semicolonRange)
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
    override fun evaluate(): Completion.Abrupt {
        val value = expression.evaluateValueOrReturn { return it }
        return Completion.Throw(value)
    }
}
