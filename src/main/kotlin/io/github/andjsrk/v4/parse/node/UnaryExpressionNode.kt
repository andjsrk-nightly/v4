package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.missingBranch
import io.github.andjsrk.v4.parse.UnaryOperationType
import io.github.andjsrk.v4.parse.UnaryOperationType.*
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
        return when (this.operation) {
            VOID -> {
                operand.evaluateValue()
                    .orReturn { return it }
                return normalNull
            }
            THROW -> {
                val value = operand.evaluateValue()
                    .orReturn { return it }
                return Completion.Throw(value)
            }
            TYPEOF -> {
                val value = operand.evaluateValue()
                    .orReturn { return it }
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
                val value = operand.evaluateValue()
                    .orReturn { return it }
                    .requireToBe<NumericType<*>> { return it }
                -value
            }
            BITWISE_NOT -> {
                val value = operand.evaluateValue()
                    .orReturn { return it }
                    .requireToBe<NumericType<*>> { return it }
                value.bitwiseNot()
                    .orReturn { return it }
            }
            NOT -> {
                val value = operand.evaluateValue()
                    .orReturn { return it }
                    .requireToBe<BooleanType> { return it }
                !value
            }
            // TODO: await expression
            else -> missingBranch()
        }
            .toNormal()
    }
}
