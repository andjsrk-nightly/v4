package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.evaluateOrReturn
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.NonEmpty
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class SequenceExpressionNode(
    val expressions: List<ExpressionNode>,
    override val range: Range,
): ExpressionNode, NonAtomicNode {
    override val childNodes = expressions
    override fun toString() =
        stringifyLikeDataClass(::expressions, ::range)
    override fun evaluate(): NonEmpty {
        return Completion.WideNormal(
            expressions
                .map { it.evaluateOrReturn { return it } }
                .last()
        )
    }
}
