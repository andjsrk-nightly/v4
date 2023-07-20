package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.parse.UnaryOperationType.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.parse.UnaryOperationType
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

open class UnaryExpressionNode(
    val operand: ExpressionNode,
    val operation: UnaryOperationType,
    operationTokenRange: Range,
    val isPrefixed: Boolean = true
): ExpressionNode, NonAtomicNode {
    override val childNodes get() = listOf(operand)
    override val range by lazy {
        if (isPrefixed) operationTokenRange..operand.range
        else operand.range..operationTokenRange
    }
    override fun toString() =
        stringifyLikeDataClass(::operand, ::operation, ::range)
    override fun evaluate(): NonEmptyNormalOrAbrupt {
        return Completion.Normal(
            when (this.operation) {
                VOID -> {
                    operand.evaluateValueOrReturn { return it }
                    return Completion.Normal.`null`
                }
                TYPEOF -> {
                    val value = operand.evaluateValueOrReturn { return it }
                    when (value) {
                        NullType -> "null"
                        is StringType -> "string"
                        is NumberType -> "number"
                        is BooleanType -> "boolean"
                        is SymbolType -> "symbol"
                        is BigIntType -> "bigint"
                        is ObjectType -> "object"
                    }
                        .languageValue
                }
                MINUS -> {
                    val value = operand.evaluateValueOrReturn { return it }
                        .requireToBe<NumericType<*>> { return it }
                    -value
                }
                BITWISE_NOT -> {
                    val value = operand.evaluateValueOrReturn { return it }
                        .requireToBe<NumericType<*>> { return it }
                    value.bitwiseNot().extractFromCompletionOrReturn { return it }
                }
                NOT -> {
                    val value = operand.evaluateValueOrReturn { return it }
                        .requireToBe<BooleanType> { return it }
                    !value
                }
                // TODO: await expression
                else -> missingBranch()
            }
        )
    }
}
