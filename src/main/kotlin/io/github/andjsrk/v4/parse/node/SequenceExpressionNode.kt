package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.EvalFlow
import io.github.andjsrk.v4.evaluate.evaluateValue
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class SequenceExpressionNode(
    val expressions: List<ExpressionNode>,
    override val range: Range,
): ExpressionNode, NonAtomicNode {
    override val childNodes = expressions
    override fun toString() =
        stringifyLikeDataClass(::expressions, ::range)
    override fun evaluate() =
        EvalFlow {
            var last: LanguageType = NullType
            expressions.forEach {
                last = it.evaluateValue()
                    .returnIfAbrupt(this) { return@EvalFlow }
            }
            `return`(last.toNormal())
        }
}
