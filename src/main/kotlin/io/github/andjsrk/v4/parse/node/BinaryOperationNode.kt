package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.BinaryOperationType
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class BinaryOperationNode(
    val left: ExpressionNode,
    val right: ExpressionNode,
    val operation: BinaryOperationType,
): ExpressionNode {
    override val range = left.range until right.range
    override fun toString() =
        stringifyLikeDataClass(::left, ::right, ::operation, ::range)
}
