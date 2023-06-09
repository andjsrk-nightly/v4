package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.BinaryOperationType
import io.github.andjsrk.v4.BinaryOperationType.ASSIGN
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.parse.*

class BinaryExpressionNode(
    val left: ExpressionNode,
    val right: ExpressionNode,
    val operation: BinaryOperationType,
): ExpressionNode, NonAtomicNode {
    override val childNodes get() = listOf(left, right)
    override val range = left.range..right.range
    override fun toString() =
        stringifyLikeDataClass(::left, ::right, ::operation, ::range)
    override fun evaluate(): NonEmptyNormalOrAbrupt {
        if (operation.isAssignLike) {
            // TODO: destructuring assignment

            val lref = left.evaluateOrReturn { return it } as Reference
            val rval =
                if (operation == ASSIGN) {
                    if (left is IdentifierNode && right.isAnonymous) right.evaluateWithNameOrReturn(left.stringValue) { return it }
                    else right.evaluateValueOrReturn { return it }
                } else {
                    val lval = getValueOrReturn(lref) { return it }
                    lval.operate(operation.toNonAssign(), right)
                        .returnIfAbrupt { return it }
                }
            lref.putValue(rval)
                .returnIfAbrupt { return it }
            return Completion.Normal(rval)
        }

        val lval = left.evaluateValueOrReturn { return it }

        return lval.operate(operation, right)
    }
}
