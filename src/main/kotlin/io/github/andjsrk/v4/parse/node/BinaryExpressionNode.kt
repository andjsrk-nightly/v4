package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.BinaryOperationType.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.evaluate.type.spec.Reference
import io.github.andjsrk.v4.parse.*

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
        if (operation.isAssignLike) {
            // TODO: destructuring assignment
            val lref = returnIfAbrupt<Reference>(left.evaluate()) { return it }
            val rval =
                if (left is IdentifierNode && right.isAnonymous) right.evaluateWithNameOrReturn(left.stringValue) { return it }
                else right.evaluateValueOrReturn { return it }
            returnIfAbrupt(lref.putValue(rval)) { return it }
            return Completion.normal(rval)
        }

        val lval = left.evaluateValueOrReturn { return it }

        when (operation) {
            AND -> {
                // NOTE: current behavior
                // evaluate left side
                // coerce left side to be a Boolean
                // if left side is `false`, return `false`
                // else,
                //   evaluate right side
                //   return right side
                if (lval !is BooleanType) return Completion(Completion.Type.THROW, NullType/* TypeError */)
                if (!lval.value) return Completion.normal(BooleanType.FALSE)
                return right.evaluateValue()
            }
            OR -> {
                // NOTE: current behavior
                // evaluate left side
                // coerce left side to be a Boolean
                // if left side is `true`, return `true`
                // else,
                //   evaluate right side
                //   coerce right side to be a Boolean
                //   return right side
                if (lval !is BooleanType) return Completion(Completion.Type.THROW, NullType/* TypeError */)
                if (lval.value) return Completion.normal(BooleanType.TRUE)
                val rval = right.evaluateValueOrReturn { return it }
                if (rval !is BooleanType) return Completion(Completion.Type.THROW, NullType/* TypeError */)
                return Completion.normal(rval)
            }
            COALESCE -> return (
                if (lval == NullType) right.evaluateValue()
                else Completion.normal(lval)
            )
            else -> {}
        }

        val rval = right.evaluateValueOrReturn { return it }

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
                EXPONENTIAL -> lval.pow(rval)
                MULTIPLY -> lval * rval
                DIVIDE -> (lval / rval)
                MOD -> (lval % rval)
                PLUS -> lval + rval
                MINUS -> lval - rval
                SHL -> lval.leftShift(rval)
                SAR -> lval.signedRightShift(rval)
                SHR -> lval.unsignedRightShift(rval)
                BITWISE_AND -> lval.bitwiseAnd(rval)
                BITWISE_XOR -> lval.bitwiseXor(rval)
                BITWISE_OR -> lval.bitwiseOr(rval)
                else -> missingBranch()
            }
                .extractFromCompletionOrReturn { return it }
        )
    }
}
