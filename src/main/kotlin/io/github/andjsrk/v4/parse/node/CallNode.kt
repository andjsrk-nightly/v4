package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

sealed class CallNode(
    callee: ExpressionNode,
    val arguments: ArgumentsNode,
): FixedArgumentCallNode(callee) {
    override val childNodes get() = super.childNodes + arguments
    override val range = callee.range..arguments.range
    override fun toString() =
        stringifyLikeDataClass(::callee, ::arguments, ::range)
    override fun evaluate() =
        EvalFlow {
            val ref = callee.evaluate().returnIfAbrupt(this) { return@EvalFlow }
            val func = getValue(ref).returnIfAbrupt { return@EvalFlow }
            val args = evaluateArguments(arguments)
                .returnIfAbrupt(this) { return@EvalFlow }
            `return`(evaluateCall(func, ref, args))
        }
}
