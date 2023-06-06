package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.UnaryOperationType.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.spec.Completion
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
    override fun evaluate(): Completion {
        when (this.operation) {
            VOID -> {
                val expr = operand.evaluateOrReturn { return it }
                getValueOrReturn(expr) { return it }
                return Completion.normal(NullType)
            }
            TYPEOF -> {
                val expr = operand.evaluateOrReturn { return it }
                val value = getValueOrReturn(expr) { return it }
                return Completion.normal(
                    StringType(
                        when (value) {
                            is NullType -> "null"
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
                val expr = operand.evaluateOrReturn { return it }
                val value = getValueOrReturn(expr) { return it }
                return when (value) {
                    is NumericType<*> -> Completion.normal(-value)
                    else -> Completion(Completion.Type.THROW, NullType)
                }
            }
            BITWISE_NOT -> {
                val expr = operand.evaluateOrReturn { return it }
                val value = getValueOrReturn(expr) { return it }
                return when (value) {
                    is NumericType<*> -> {
                        val res = value.bitwiseNot().extractIfCompletion { return it }
                        Completion.normal(res)
                    }
                    else -> Completion(Completion.Type.THROW, NullType/* TypeError */)
                }
            }
            NOT -> {
                val expr = operand.evaluateOrReturn { return it }
                val value = getValueOrReturn(expr) { return it }
                if (value !is BooleanType) return Completion(Completion.Type.THROW, NullType/* TypeError */)
                return Completion.normal(!value)
            }
            // TODO: await expression
            else -> missingBranch()
        }
    }
}
