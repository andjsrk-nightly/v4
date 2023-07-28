package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.BinaryOperationType.ASSIGN
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
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
    override fun evaluate(): NonEmptyNormalOrAbrupt {
        if (operation.isAssignLike) {
            // TODO: destructuring assignment

            val lref = left.evaluateOrReturn { return it } as Reference
            val rval =
                if (operation == ASSIGN) {
                    if (left is IdentifierNode && right.isAnonymous) right.evaluateWithNameOrReturn(left.stringValue) { return it }
                    else right.evaluateValueOrReturn { return it }
                } else {
                    val lval = getValueOrReturn(lref) { return it }
                    lval.operate(operation.toNonAssign(), right)
                        .returnIfAbrupt { return it }
                }
            lref.putValue(rval)
                .returnIfAbrupt { return it }
            return Completion.Normal(rval)
        }

        val lval = left.evaluateValueOrReturn { return it }

        return lval.operate(operation, right)
    }
}

/**
 * Applies binary operator except assignments.
 *
 * Note that [other] is not a [LanguageType] because right side needs to be evaluated conditionally for some operations.
 */
internal fun LanguageType.operate(operation: BinaryOperationType, other: ExpressionNode): NonEmptyNormalOrAbrupt {
    assert(operation.not { isAssignLike })

    val left = this

    when (operation) {
        BinaryOperationType.AND -> {
            // NOTE: current behavior
            // evaluate left side
            // require left side to be a Boolean
            // if left side is `false`, return `false`
            // else,
            //   evaluate right side
            //   require right side to be a Boolean
            //   return right side
            val booleanLeft = left.requireToBe<BooleanType> { return it }
            if (!booleanLeft.value) return Completion.Normal(BooleanType.FALSE)
            val right = other.evaluateValueOrReturn { return it }
                .requireToBe<BooleanType> { return it }
            return Completion.Normal(right)
        }
        BinaryOperationType.THEN -> {
            val booleanLeft = left.requireToBe<BooleanType> { return it }
            if (!booleanLeft.value) return Completion.Normal(BooleanType.FALSE)
            return other.evaluateValue()
        }
        BinaryOperationType.OR -> {
            // NOTE: current behavior
            // evaluate left side
            // require left side to be a Boolean
            // if left side is `true`, return `true`
            // else,
            //   evaluate right side
            //   require right side to be a Boolean
            //   return right side
            val booleanLeft = left.requireToBe<BooleanType> { return it }
            if (booleanLeft.value) return Completion.Normal(BooleanType.TRUE)
            val right = other.evaluateValueOrReturn { return it }
                .requireToBe<BooleanType> { return it }
            return Completion.Normal(right)
        }
        BinaryOperationType.COALESCE -> return (
                if (left == NullType) other.evaluateValue()
                else Completion.Normal(left)
                )
        else -> {}
    }

    val right = other.evaluateValueOrReturn { return it }

    when (operation) {
        BinaryOperationType.LT, BinaryOperationType.GT, BinaryOperationType.LT_EQ, BinaryOperationType.GT_EQ ->
            return if (left is StringType || left is NumericType<*>) {
                if (left::class != right::class) throwError(TypeErrorKind.LHS_RHS_NOT_SAME_TYPE)
                else Completion.Normal(
                    when (operation) {
                        BinaryOperationType.LT -> left.isLessThan(right, BooleanType.FALSE)
                        BinaryOperationType.GT -> right.isLessThan(left, BooleanType.FALSE)
                        BinaryOperationType.LT_EQ -> !right.isLessThan(left, BooleanType.TRUE)
                        BinaryOperationType.GT_EQ -> !left.isLessThan(right, BooleanType.TRUE)
                        else -> neverHappens()
                    }
                )
            }
            else unexpectedType(left, StringType::class, NumericType::class)
        BinaryOperationType.PLUS -> {
            val leftAsString = left as? StringType
            val rightAsString = right as? StringType
            if (leftAsString != null || rightAsString != null) {
                val stringLeft = leftAsString ?: stringify(left).returnIfAbrupt { return it }
                val stringRight = rightAsString ?: stringify(right).returnIfAbrupt { return it }
                return Completion.Normal(stringLeft + stringRight)
            }
            // numeric values will be handled on below
        }
        BinaryOperationType.EQ -> return Completion.Normal(equal(left, right).languageValue)
        BinaryOperationType.NOT_EQ -> return Completion.Normal(!equal(left, right).languageValue)
        else -> {}
    }

    val numericLeft = left.requireToBe<NumericType<*>> { return it }
    val numericRight = right.requireToBe<NumericType<*>> { return it }
    if (left::class != right::class) return throwError(TypeErrorKind.BIGINT_MIXED_TYPES)

    return Completion.Normal(
        @CompilerFalsePositive
        @Suppress("TYPE_MISMATCH")
        when (operation) {
            BinaryOperationType.EXPONENTIAL -> numericLeft.pow(numericRight)
            BinaryOperationType.MULTIPLY -> numericLeft * numericRight
            BinaryOperationType.DIVIDE -> numericLeft / numericRight
            BinaryOperationType.MOD ->
                @CompilerFalsePositive
                @Suppress("UNRESOLVED_REFERENCE_WRONG_RECEIVER")
                numericLeft % numericRight
            BinaryOperationType.PLUS -> numericLeft + numericRight
            BinaryOperationType.MINUS -> numericLeft - numericRight
            BinaryOperationType.SHL -> numericLeft.leftShift(numericRight)
            BinaryOperationType.SAR -> numericLeft.signedRightShift(numericRight)
            BinaryOperationType.SHR -> numericLeft.unsignedRightShift(numericRight)
            BinaryOperationType.BITWISE_AND -> numericLeft.bitwiseAnd(numericRight)
            BinaryOperationType.BITWISE_XOR -> numericLeft.bitwiseXor(numericRight)
            BinaryOperationType.BITWISE_OR -> numericLeft.bitwiseOr(numericRight)
            else -> missingBranch()
        }
            .extractFromCompletionOrReturn { return it }
    )
}
