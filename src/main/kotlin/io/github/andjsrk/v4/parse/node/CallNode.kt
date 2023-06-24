package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

sealed class CallNode(
    callee: ExpressionNode,
    val arguments: ArgumentsNode,
): FixedArgumentCallNode(callee) {
    override val childNodes get() = super.childNodes + arguments
    override val range = callee.range..arguments.range
    override fun toString() =
        stringifyLikeDataClass(::callee, ::arguments, ::range)
    override fun evaluate(): NonEmptyNormalOrAbrupt {
        val ref = callee.evaluateOrReturn { return it }
        val func = getValueOrReturn(ref) { return it }
        val args = returnIfAbrupt(evaluateArguments(arguments)) { return it }
        return evaluateCall(func, ref, args)
    }
}
