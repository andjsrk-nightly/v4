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
    override fun evaluate() = lazyFlow f@ {
        val ref = yieldAll(callee.evaluate())
            .orReturn { return@f it }
        val func = getValue(ref)
            .orReturn { return@f it }
        val args = yieldAll(arguments.evaluate())
            .orReturn { return@f it }
        evaluateCall(func, ref, args)
    }
}
