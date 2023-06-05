package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.UnaryOperationType
import io.github.andjsrk.v4.UnaryOperationType.*
import io.github.andjsrk.v4.evaluate.getValueOrReturn
import io.github.andjsrk.v4.evaluate.returnIfAbrupt
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
                val expr = returnIfAbrupt(operand.evaluate()) { return it }
                getValueOrReturn(expr) { return it }
                return Completion.normal(NullType)
            }
            TYPEOF -> {
                val expr = returnIfAbrupt(operand.evaluate()) { return it }
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
                val expr = returnIfAbrupt(operand.evaluate()) { return it }
                val value = getValueOrReturn(expr) { return it }
                return when (value) {
                    is NumericType<*> -> Completion.normal(-value)
                    else -> Completion(Completion.Type.THROW, NullType)
                }
            }
            BITWISE_NOT -> {
                val expr = returnIfAbrupt(operand.evaluate()) { return it }
                val value = getValueOrReturn(expr) { return it }
                return when (value) {
                    is NumberType -> {
                        val res = returnIfAbrupt(value.bitwiseNot()) { return it }
                        return Completion.normal(res)
                    }
                    is BigIntType -> Completion.normal(value.bitwiseNot())
                    is NumericType<*> -> TODO()
                    else -> Completion(Completion.Type.THROW, NullType/* TypeError */)
                }
            }
            else -> TODO()
        }
    }
}
