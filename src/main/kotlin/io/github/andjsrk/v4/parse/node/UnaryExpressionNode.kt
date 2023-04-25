package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.UnaryOperationType
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

open class UnaryExpressionNode(
    val operand: ExpressionNode,
    val operation: UnaryOperationType,
    operationTokenRange: Range,
    val isPrefixed: Boolean = true
): ExpressionNode {
    override val range =
        if (isPrefixed) operationTokenRange..operand.range
        else operand.range..operationTokenRange
    override fun toString() =
        stringifyLikeDataClass(::operand, ::operation, ::range)
}
