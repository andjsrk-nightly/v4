package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
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
    override fun evaluate() = lazyFlow f@ {
        val value = yieldAll(operand.evaluateValue())
            .orReturn { return@f it }
        when (operation) {
            VOID -> return@f normalNull
            THROW -> return@f Completion.Throw(value)
            TYPEOF -> when (value) {
                NullType -> "null"
                is StringType -> "string"
                is NumberType -> "number"
                is BooleanType -> "boolean"
                is SymbolType -> "symbol"
                is BigIntType -> "bigint"
                is ObjectType -> "object"
            }
                .languageValue
            MINUS -> {
                val number = value
                    .requireToBe<NumericType<*>> { return@f it }
                -number
            }
            BITWISE_NOT -> {
                val number = value
                    .requireToBe<NumericType<*>> { return@f it }
                return@f number.bitwiseNot()
            }
            NOT -> {
                val boolean = value
                    .requireToBe<BooleanType> { return@f it }
                !boolean
            }
            AWAIT -> return@f yieldAll(await(value))
            else -> missingBranch()
        }
            .toNormal()
    }
}
