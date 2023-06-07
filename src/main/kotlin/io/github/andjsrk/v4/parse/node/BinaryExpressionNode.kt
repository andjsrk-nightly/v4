package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.BinaryOperationType.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.spec.Completion
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
        val lref = left.evaluateOrReturn { return it }
        val lval = getValueOrReturn(lref) { return it }
        val rref = right.evaluateOrReturn { return it }
        val rval = getValueOrReturn(rref) { return it }

        when (operation) {
            LT, GT, LT_EQ, GT_EQ -> return when {
                lval::class != rval::class -> Completion(Completion.Type.THROW, NullType/* TypeError */)
                lval is StringType || lval is NumericType<*> ->
                    BooleanType.run {
                        when (operation) {
                            LT -> lval.isLessThan(rval, FALSE)
                            GT -> rval.isLessThan(lval, FALSE)
                            LT_EQ -> rval.isLessThan(lval, TRUE).map { !(it as BooleanType) }
                            GT_EQ -> lval.isLessThan(rval, TRUE).map { !(it as BooleanType) }
                            else -> neverHappens()
                        }
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
            EQ -> return Completion.normal(equal(lval, rval))
            NOT_EQ -> return Completion.normal(!equal(lval, rval))
            else -> {}
        }

        if (lval !is NumericType<*>) return Completion(Completion.Type.THROW, NullType/* TypeError */)
        if (rval !is NumericType<*>) return Completion(Completion.Type.THROW, NullType/* TypeError */)
        if (lval::class != rval::class) return Completion(Completion.Type.THROW, NullType/* TypeError */)

        return Completion.normal(
            when (operation) {
                EXPONENTIAL -> lval.pow(rval).extractIfCompletion { return it }
                MULTIPLY -> lval * rval
                DIVIDE -> (lval / rval).extractIfCompletion { return it }
                MOD -> (lval % rval).extractIfCompletion { return it }
                PLUS -> lval + rval
                MINUS -> lval - rval
                SHL -> lval.leftShift(rval).extractIfCompletion { return it }
                SAR -> lval.signedRightShift(rval).extractIfCompletion { return it }
                SHR -> lval.unsignedRightShift(rval).extractIfCompletion { return it }
                BITWISE_AND -> lval.bitwiseAnd(rval).extractIfCompletion { return it }
                BITWISE_XOR -> lval.bitwiseXor(rval).extractIfCompletion { return it }
                BITWISE_OR -> lval.bitwiseOr(rval).extractIfCompletion { return it }
                else -> missingBranch()
            }
        )
    }
}
