package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.NonEmptyOrAbrupt
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

sealed class CallNode(
    callee: ExpressionNode,
    val arguments: ArgumentsNode,
): FixedArgumentCallNode(callee) {
    override val childNodes get() = super.childNodes + arguments
    override val range = callee.range..arguments.range
    override fun toString() =
        stringifyLikeDataClass(::callee, ::arguments, ::range)
    override fun evaluate(): NonEmptyOrAbrupt {
        val ref = callee.evaluate()
            .orReturn { return it }
        val func = getValue(ref)
            .orReturn { return it }
        val args = arguments.evaluate()
            .orReturn { return it }
        return evaluateCall(func, ref, args)
    }
}
