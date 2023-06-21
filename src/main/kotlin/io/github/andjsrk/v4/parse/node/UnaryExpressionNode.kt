package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.UnaryOperationType.*
import io.github.andjsrk.v4.evaluate.evaluateValueOrReturn
import io.github.andjsrk.v4.evaluate.extractFromCompletionOrReturn
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
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
        when (this.operation) {
            VOID -> {
                operand.evaluateValueOrReturn { return it }
                return Completion.Normal.`null`
            }
            TYPEOF -> {
                val value = operand.evaluateValueOrReturn { return it }
                return Completion.Normal(
                    StringType(
                        when (value) {
                            NullType -> "null"
                            is StringType -> "string"
                            is NumberType -> "number"
                            is BooleanType -> "boolean"
                            is SymbolType -> "symbol"
                            is BigIntType -> "bigint"
                            is ObjectType -> "object"
                        }
                    )
                )
            }
            MINUS -> {
                val value = operand.evaluateValueOrReturn { return it }
                return when (value) {
                    is NumericType<*> -> Completion.Normal(-value)
                    else -> Completion.Throw(NullType)
                }
            }
            BITWISE_NOT -> {
                val value = operand.evaluateValueOrReturn { return it }
                return when (value) {
                    is NumericType<*> -> {
                        val res = value.bitwiseNot().extractFromCompletionOrReturn { return it }
                        Completion.Normal(res)
                    }
                    else -> Completion.Throw(NullType/* TypeError */)
                }
            }
            NOT -> {
                val value = operand.evaluateValueOrReturn { return it }
                if (value !is BooleanType) return Completion.Throw(NullType/* TypeError */)
                return Completion.Normal(!value)
            }
            // TODO: await expression
            else -> missingBranch()
        }
    }
}
