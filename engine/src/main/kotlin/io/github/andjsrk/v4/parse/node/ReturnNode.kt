package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.NullType
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ReturnNode(
    val expression: ExpressionNode?,
    startRange: Range,
): ExpressionNode, NonAtomicNode {
    override val childNodes get() = listOf(expression)
    override val range = startRange.extendCarefully(expression?.range)
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
    override fun evaluate() = lazyFlow f@ {
        val value = expression?.evaluateValue()
            ?.let { yieldAll(it) }
            ?.orReturn { return@f it }
            ?: NullType
        Completion.Return(value)
    }
}
