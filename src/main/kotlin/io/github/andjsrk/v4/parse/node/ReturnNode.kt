package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.evaluateValue
import io.github.andjsrk.v4.evaluate.orReturn
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ReturnNode(
    val expression: ExpressionNode?,
    startRange: Range,
    semicolonRange: Range?,
): StatementNode, NonAtomicNode {
    override val childNodes get() = listOf(expression)
    override val range = startRange..(semicolonRange ?: expression?.range ?: startRange)
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
    override fun evaluate(): Completion.Abrupt {
        val value = expression?.evaluateValue()?.orReturn { return it } ?: NullType
        return Completion.Return(value)
    }
}
