package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
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
    override fun evaluate() =
        EvalFlow<LanguageType> {
            val value = expression?.let {
                val value = it.evaluateValue()
                    .returnIfAbrupt(this) { return@EvalFlow }
                if (generatorKind == GeneratorKind.ASYNC) TODO()
                value
            }
            Completion.Return(value ?: NullType)
        }
}
