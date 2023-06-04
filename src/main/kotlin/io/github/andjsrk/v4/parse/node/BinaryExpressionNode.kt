package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.BinaryOperationType
import io.github.andjsrk.v4.BinaryOperationType.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.neverHappens
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class BinaryExpressionNode(
    val left: ExpressionNode,
    val right: ExpressionNode,
    val operation: BinaryOperationType,
): ExpressionNode, NonAtomicNode {
    override val childNodes get() = listOf(left, right)
    override val range = left.range..right.range
    override fun toString() =
        stringifyLikeDataClass(::left, ::right, ::operation, ::range)
    override fun evaluate(): Completion {
        val lref = returnIfAbrupt(left.evaluate()) { return it }
        val lval = getValueOrReturn(lref) { return it }
        val rref = returnIfAbrupt(right.evaluate()) { return it }
        val rval = getValueOrReturn(rref) { return it }

        when (operation) {
            LT, GT, LT_EQ, GT_EQ -> return when {
                lval::class != rval::class -> Completion(Completion.Type.THROW, NullType/* TypeError */)
                lval is StringType || lval is NumericType<*> ->
                    when (operation) {
                        LT -> lval.isLessThan(rval, BooleanType.FALSE)
                        GT -> rval.isLessThan(lval, BooleanType.FALSE)
                        LT_EQ -> rval.isLessThan(lval, BooleanType.TRUE).map { !(it as BooleanType) }
                        GT_EQ -> lval.isLessThan(rval, BooleanType.TRUE).map { !(it as BooleanType) }
                        else -> neverHappens()
                    }
                else -> Completion(Completion.Type.THROW, NullType/* TypeError */)
            }
            PLUS -> {
                val leftAsString = lval as? StringType
                val rightAsString = rval as? StringType
                if (leftAsString != null || rightAsString != null) {
                    val left = leftAsString ?: returnIfAbrupt<StringType>(toString(lval)) { return it }
                    val right = rightAsString ?: returnIfAbrupt<StringType>(toString(rval)) { return it }
                    return Completion.normal(left + right)
                }
            }
            else -> {}
        }

        if (lval !is NumericType<*>) return Completion(Completion.Type.THROW, NullType)
        if (rval !is NumericType<*>) return Completion(Completion.Type.THROW, NullType)
        if (lval::class != rval::class) return Completion(Completion.Type.THROW, NullType)

        // it seems that Kotlin cannot handle pattern like `NumericType`,
        // so we need to write code for each concrete types
        when (lval) {
            is NumberType -> {
                val rval = rval as NumberType
                return Completion.normal(
                    when (operation) {
                        PLUS -> lval + rval
                        MINUS -> lval - rval
                        MULTIPLY -> lval * rval
                        else -> TODO()
                    }
                )
            }
            is BigIntType -> {
                val rval = rval as BigIntType
                return Completion.normal(
                    when (operation) {
                        PLUS -> lval + rval
                        MINUS -> lval - rval
                        MULTIPLY -> lval * rval
                        else -> TODO()
                    }
                )
            }
        }
    }
}
