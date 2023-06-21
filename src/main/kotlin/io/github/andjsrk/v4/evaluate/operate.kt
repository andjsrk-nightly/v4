package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.BinaryOperationType.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType.Companion.FALSE
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType.Companion.TRUE
import io.github.andjsrk.v4.parse.node.ExpressionNode

/**
 * Applies binary operator except assignments.
 *
 * Note that [other] is not a [LanguageType] because right side needs to be evaluated conditionally for some operations.
 */
internal fun LanguageType.operate(operation: BinaryOperationType, other: ExpressionNode): NonEmptyNormalOrAbrupt {
    assert(operation.not { isAssignLike })

    val lval = this

    when (operation) {
        AND -> {
            // NOTE: current behavior
            // evaluate left side
            // coerce left side to be a Boolean
            // if left side is `false`, return `false`
            // else,
            //   evaluate right side
            //   return right side
            if (lval !is BooleanType) return Completion.Throw(NullType/* TypeError */)
            if (!lval.value) return Completion.Normal(FALSE)
            return other.evaluateValue()
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
            if (lval !is BooleanType) return Completion.Throw(NullType/* TypeError */)
            if (lval.value) return Completion.Normal(TRUE)
            val rval = other.evaluateValueOrReturn { return it }
            if (rval !is BooleanType) return Completion.Throw(NullType/* TypeError */)
            return Completion.Normal(rval)
        }
        COALESCE -> return (
            if (lval == NullType) other.evaluateValue()
            else Completion.Normal(lval)
        )
        else -> {}
    }

    val rval = other.evaluateValueOrReturn { return it }

    when (operation) {
        LT, GT, LT_EQ, GT_EQ -> return when {
            lval::class != rval::class -> Completion.Throw(NullType/* TypeError */)
            lval is StringType || lval is NumericType<*> -> Completion.Normal(
                when (operation) {
                    LT -> lval.isLessThan(rval, FALSE)
                    GT -> rval.isLessThan(lval, FALSE)
                    LT_EQ -> !rval.isLessThan(lval, TRUE)
                    GT_EQ -> !lval.isLessThan(rval, TRUE)
                    else -> neverHappens()
                }
            )
            else -> Completion.Throw(NullType/* TypeError */)
        }
        PLUS -> {
            val leftAsString = lval as? StringType
            val rightAsString = rval as? StringType
            if (leftAsString != null || rightAsString != null) {
                val left = leftAsString ?: returnIfAbrupt(toString(lval)) { return it }
                val right = rightAsString ?: returnIfAbrupt(toString(rval)) { return it }
                return Completion.Normal(left + right)
            }
            // numeric values will be handled on below
        }
        EQ -> return Completion.Normal(equal(lval, rval))
        NOT_EQ -> return Completion.Normal(!equal(lval, rval))
        else -> {}
    }

    if (lval !is NumericType<*>) return Completion.Throw(NullType/* TypeError */)
    if (rval !is NumericType<*>) return Completion.Throw(NullType/* TypeError */)
    if (lval::class != rval::class) return Completion.Throw(NullType/* TypeError */)

    return Completion.Normal(
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
