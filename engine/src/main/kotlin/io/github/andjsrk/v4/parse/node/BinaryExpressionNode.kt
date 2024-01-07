package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.parse.*
import io.github.andjsrk.v4.BinaryOperationType as BinaryOpType

class BinaryExpressionNode(
    val left: ExpressionNode,
    val right: ExpressionNode,
    val operation: BinaryOpType,
): ExpressionNode, NonAtomicNode {
    override val childNodes get() = listOf(left, right)
    override val range = left.range..right.range
    override fun toString() =
        stringifyLikeDataClass(::left, ::right, ::operation, ::range)
    override fun evaluate(): NonEmptyOrAbrupt {
        if (operation.isAssignLike) {
            // TODO: destructuring assignment

            val lref = left.evaluate().orReturn { return it } as Reference
            val rval =
                if (operation == BinaryOpType.ASSIGN) {
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

        val lval = left.evaluateValue()
            .orReturn { return it }

        return lval.operate(operation, right)
    }
}

/**
 * Applies binary operator except assignments.
 *
 * Note that [other] is not a [LanguageType] because right side needs to be evaluated conditionally for some operations.
 */
internal fun LanguageType.operate(operation: BinaryOpType, other: ExpressionNode): NonEmptyOrAbrupt {
    assert(operation.not { isAssignLike })

    val left = this

    when (operation) {
        BinaryOpType.AND -> {
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
        BinaryOpType.THEN -> {
            val booleanLeft = left.requireToBe<BooleanType> { return it }
            if (!booleanLeft.value) return BooleanType.FALSE.toNormal()
            return other.evaluateValue()
        }
        BinaryOpType.OR -> {
            val booleanLeft = left.requireToBe<BooleanType> { return it }
            if (booleanLeft.value) return BooleanType.TRUE.toNormal()
            val right = other.evaluateValue()
                .orReturn { return it }
                .requireToBe<BooleanType> { return it }
            return right.toNormal()
        }
        BinaryOpType.COALESCE -> return (
            if (left == NullType) other.evaluateValue()
            else left.toNormal()
        )
        else -> {}
    }

    val right = other.evaluateValue()
        .orReturn { return it }

    when (operation) {
        BinaryOpType.LT -> return left.lessThan(right)
        BinaryOpType.GT -> return right.lessThan(left)
        BinaryOpType.LT_EQ -> {
            val greater = right.lessThan(left)
                .orReturn { return it }
            return (!greater).toNormal()
        }
        BinaryOpType.GT_EQ -> {
            val less = left.lessThan(right)
                .orReturn { return it }
            return (!less).toNormal()
        }
        BinaryOpType.PLUS -> {
            val leftAsString = left as? StringType
            val rightAsString = right as? StringType
            if (leftAsString != null || rightAsString != null) {
                val stringLeft = leftAsString
                    ?: stringify(left)
                        .orReturn { return it }
                val stringRight = rightAsString
                    ?: stringify(right)
                        .orReturn { return it }
                return (stringLeft + stringRight).toNormal()
            }
            // numeric values will be handled below
        }
        BinaryOpType.EQ ->
            return equal(left, right)
                .languageValue
                .toNormal()
        BinaryOpType.NOT_EQ ->
            return not { equal(left, right) }
                .languageValue
                .toNormal()
        BinaryOpType.IN -> {
            val key = left.requireToBePropertyKey { return it }
            return right.hasProperty(key)
        }
        BinaryOpType.INSTANCEOF -> {
            if (right !is ClassType) return throwError(TypeErrorKind.INSTANCEOF_RHS_IS_NOT_CLASS)
            // there is no @@hasInstance
            if (left !is ObjectType) return BooleanType.FALSE.toNormal()
            return left.isInstanceOf(right)
                .languageValue
                .toNormal()
        }
        else -> {}
    }

    val numericLeft = left.requireToBe<NumericType<*>> { return it }
    val numericRight = right.requireToBe<NumericType<*>> { return it }
    if (left::class != right::class) return throwError(TypeErrorKind.BIGINT_MIXED_TYPES)

    return (
        @CompilerFalsePositive
        @Suppress("TYPE_MISMATCH")
        when (operation) {
            BinaryOpType.EXPONENTIAL -> numericLeft.pow(numericRight)
            BinaryOpType.MULTIPLY -> (numericLeft * numericRight).toNormal()
            BinaryOpType.DIVIDE -> numericLeft / numericRight
            BinaryOpType.MOD ->
                @CompilerFalsePositive
                @Suppress("OPERATOR_MODIFIER_REQUIRED")
                (numericLeft % numericRight)
            BinaryOpType.PLUS -> (numericLeft + numericRight).toNormal()
            BinaryOpType.MINUS -> (numericLeft - numericRight).toNormal()
            BinaryOpType.SHL -> numericLeft.leftShift(numericRight)
            BinaryOpType.SAR -> numericLeft.signedRightShift(numericRight)
            BinaryOpType.SHR -> numericLeft.unsignedRightShift(numericRight)
            BinaryOpType.BITWISE_AND -> numericLeft.bitwiseAnd(numericRight)
            BinaryOpType.BITWISE_XOR -> numericLeft.bitwiseXor(numericRight)
            BinaryOpType.BITWISE_OR -> numericLeft.bitwiseOr(numericRight)
            else -> missingBranch()
        }
    )
}
