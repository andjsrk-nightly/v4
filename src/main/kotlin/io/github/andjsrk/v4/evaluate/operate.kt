package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.BinaryOperationType.*
import io.github.andjsrk.v4.error.TypeErrorKind
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

    val left = this

    when (operation) {
        AND -> {
            // NOTE: current behavior
            // evaluate left side
            // coerce left side to be a Boolean
            // if left side is `false`, return `false`
            // else,
            //   evaluate right side
            //   return right side
            val booleanLeft = left
                .requireToBe<BooleanType> { return it }
            if (!booleanLeft.value) return Completion.Normal(FALSE)
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
            val booleanLeft = left
                .requireToBe<BooleanType> { return it }
            if (booleanLeft.value) return Completion.Normal(TRUE)
            val right = other.evaluateValueOrReturn { return it }
                .requireToBe<BooleanType> { return it }
            return Completion.Normal(right)
        }
        COALESCE -> return (
            if (left == NullType) other.evaluateValue()
            else Completion.Normal(left)
        )
        else -> {}
    }

    val right = other.evaluateValueOrReturn { return it }

    when (operation) {
        LT, GT, LT_EQ, GT_EQ ->
            return if (left is StringType || left is NumericType<*>) {
                if (left::class != right::class) throwError(TypeErrorKind.LHS_RHS_NOT_SAME_TYPE)
                else Completion.Normal(
                    when (operation) {
                        LT -> left.isLessThan(right, FALSE)
                        GT -> right.isLessThan(left, FALSE)
                        LT_EQ -> !right.isLessThan(left, TRUE)
                        GT_EQ -> !left.isLessThan(right, TRUE)
                        else -> neverHappens()
                    }
                )
            }
            else throwError(
                TypeErrorKind.UNEXPECTED_TYPE,
                "${generalizedDescriptionOf<StringType>()} or ${generalizedDescriptionOf<NumericType<*>>()}",
                generalizedDescriptionOf(left)
            )
        PLUS -> {
            val leftAsString = left as? StringType
            val rightAsString = right as? StringType
            if (leftAsString != null || rightAsString != null) {
                val stringLeft = leftAsString ?: returnIfAbrupt(stringify(left)) { return it }
                val stringRight = rightAsString ?: returnIfAbrupt(stringify(right)) { return it }
                return Completion.Normal(stringLeft + stringRight)
            }
            // numeric values will be handled on below
        }
        EQ -> return Completion.Normal(equal(left, right))
        NOT_EQ -> return Completion.Normal(!equal(left, right))
        else -> {}
    }

    val numericLeft = left
        .requireToBe<NumericType<*>> { return it }
    val numericRight = right
        .requireToBe<NumericType<*>> { return it }
    if (left::class != right::class) return throwError(TypeErrorKind.BIGINT_MIXED_TYPES)

    return Completion.Normal(
        @CompilerFalsePositive
        @Suppress("TYPE_MISMATCH")
        when (operation) {
            EXPONENTIAL -> numericLeft.pow(numericRight)
            MULTIPLY -> numericLeft * numericRight
            DIVIDE -> numericLeft / numericRight
            MOD -> numericLeft % numericRight
            PLUS -> numericLeft + numericRight
            MINUS -> numericLeft - numericRight
            SHL -> numericLeft.leftShift(numericRight)
            SAR -> numericLeft.signedRightShift(numericRight)
            SHR -> numericLeft.unsignedRightShift(numericRight)
            BITWISE_AND -> numericLeft.bitwiseAnd(numericRight)
            BITWISE_XOR -> numericLeft.bitwiseXor(numericRight)
            BITWISE_OR -> numericLeft.bitwiseOr(numericRight)
            else -> missingBranch()
        }
            .extractFromCompletionOrReturn { return it }
    )
}
