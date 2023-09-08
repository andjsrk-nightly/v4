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

            val lref = left.evaluate().orReturn { return it } as Reference
            val rval =
                if (operation == ASSIGN) {
                    if (left is IdentifierNode && right.isAnonymous) right.evaluateWithName(left.stringValue)
                    else right.evaluateValue().orReturn { return it }
                } else {
                    val lval = getValue(lref).orReturn { return it }
                    lval.operate(operation.toNonAssign(), right)
                        .orReturn { return it }
                }
            lref.putValue(rval)
                .orReturn { return it }
            return rval.toNormal()
        }

        val lval = left.evaluateValue().orReturn { return it }

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
            if (!booleanLeft.value) return BooleanType.FALSE.toNormal()
            val right = other.evaluateValue()
                .orReturn { return it }
                .requireToBe<BooleanType> { return it }
            return right.toNormal()
        }
        BinaryOperationType.THEN -> {
            val booleanLeft = left.requireToBe<BooleanType> { return it }
            if (!booleanLeft.value) return BooleanType.FALSE.toNormal()
            return other.evaluateValue()
        }
        BinaryOperationType.OR -> {
            val booleanLeft = left.requireToBe<BooleanType> { return it }
            if (booleanLeft.value) return BooleanType.TRUE.toNormal()
            val right = other.evaluateValue().orReturn { return it }
                .requireToBe<BooleanType> { return it }
            return right.toNormal()
        }
        BinaryOperationType.COALESCE -> return (
            if (left == NullType) other.evaluateValue()
            else left.toNormal()
        )
        else -> {}
    }

    val right = other.evaluateValue().orReturn { return it }

    when (operation) {
        BinaryOperationType.LT,
        BinaryOperationType.GT,
        BinaryOperationType.LT_EQ,
        BinaryOperationType.GT_EQ
        -> return when (operation) {
            BinaryOperationType.LT -> left.lessThan(right)
            BinaryOperationType.GT -> right.lessThan(left)
            BinaryOperationType.LT_EQ -> {
                val greater = right.lessThan(left).orReturn { return it }
                (!greater).toNormal()
            }
            BinaryOperationType.GT_EQ -> {
                val less = left.lessThan(right).orReturn { return it }
                (!less).toNormal()
            }
            else -> neverHappens()
        }
        BinaryOperationType.PLUS -> {
            val leftAsString = left as? StringType
            val rightAsString = right as? StringType
            if (leftAsString != null || rightAsString != null) {
                val stringLeft = leftAsString ?: stringify(left).orReturn { return it }
                val stringRight = rightAsString ?: stringify(right).orReturn { return it }
                return (stringLeft + stringRight).toNormal()
            }
            // numeric values will be handled on below
        }
        BinaryOperationType.EQ ->
            return equal(left, right)
                .languageValue
                .toNormal()
        BinaryOperationType.NOT_EQ ->
            return not { equal(left, right) }
                .languageValue
                .toNormal()
        else -> {}
    }

    val numericLeft = left.requireToBe<NumericType<*>> { return it }
    val numericRight = right.requireToBe<NumericType<*>> { return it }
    if (left::class != right::class) return throwError(TypeErrorKind.BIGINT_MIXED_TYPES)

    return (
        @CompilerFalsePositive
        @Suppress("TYPE_MISMATCH")
        when (operation) {
            BinaryOperationType.EXPONENTIAL -> numericLeft.pow(numericRight)
            BinaryOperationType.MULTIPLY -> (numericLeft * numericRight).toNormal()
            BinaryOperationType.DIVIDE -> numericLeft / numericRight
            BinaryOperationType.MOD ->
                @CompilerFalsePositive
                @Suppress("OPERATOR_MODIFIER_REQUIRED")
                (numericLeft % numericRight)
            BinaryOperationType.PLUS -> (numericLeft + numericRight).toNormal()
            BinaryOperationType.MINUS -> (numericLeft - numericRight).toNormal()
            BinaryOperationType.SHL -> numericLeft.leftShift(numericRight)
            BinaryOperationType.SAR -> numericLeft.signedRightShift(numericRight)
            BinaryOperationType.SHR -> numericLeft.unsignedRightShift(numericRight)
            BinaryOperationType.BITWISE_AND -> numericLeft.bitwiseAnd(numericRight)
            BinaryOperationType.BITWISE_XOR -> numericLeft.bitwiseXor(numericRight)
            BinaryOperationType.BITWISE_OR -> numericLeft.bitwiseOr(numericRight)
            else -> missingBranch()
        }
    )
}
