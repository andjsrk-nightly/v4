package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Reference
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal
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
    override fun evaluate() = lazyFlow f@ {
        if (operation.isAssignLike) {
            // TODO: destructuring assignment

            val lref = yieldAll(left.evaluate())
                .orReturn { return@f it } as Reference
            val rval =
                if (operation == BinaryOpType.ASSIGN) {
                    if (left is IdentifierNode && right.isAnonymous) right.evaluateWithName(left.stringValue)
                    else yieldAll(right.evaluateValue())
                        .orReturn { return@f it }
                } else {
                    val lval = getValue(lref)
                        .orReturn { return@f it }
                    yieldAll(lval.operate(operation.toNonAssign(), right))
                        .orReturn { return@f it }
                }
            lref.putValue(rval)
                .orReturn { return@f it }
            return@f rval.toNormal()
        }

        if (operation == BinaryOpType.IN && left is IdentifierNode && left.isPrivateName) {
            val rval = yieldAll(right.evaluateValue())
                .orReturn { return@f it }
            val privEnv = runningExecutionContext.privateEnv
            requireNotNull(privEnv)
            val privName = resolvePrivateIdentifier(left.value, privEnv)
            return@f (rval is ObjectType && privName in rval.privateElements)
                .languageValue
                .toNormal()
        }

        val lval = yieldAll(left.evaluateValue())
            .orReturn { return@f it }

        yieldAll(lval.operate(operation, right))
    }
}

/**
 * Applies binary operator except assignments.
 *
 * Note that [other] is not a [LanguageType] because right side needs to be evaluated conditionally for some operations.
 */
internal fun LanguageType.operate(operation: BinaryOpType, other: ExpressionNode) = lazyFlow f@ {
    assert(operation.not { isAssignLike })

    val left = this@operate

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
            val booleanLeft = left.requireToBe<BooleanType> { return@f it }
            if (!booleanLeft.value) return@f BooleanType.FALSE.toNormal()
            val right = yieldAll(other.evaluateValue())
                .orReturn { return@f it }
                .requireToBe<BooleanType> { return@f it }
            return@f right.toNormal()
        }
        BinaryOpType.THEN -> {
            val booleanLeft = left.requireToBe<BooleanType> { return@f it }
            if (!booleanLeft.value) return@f BooleanType.FALSE.toNormal()
            return@f yieldAll(other.evaluateValue())
        }
        BinaryOpType.OR -> {
            val booleanLeft = left.requireToBe<BooleanType> { return@f it }
            if (booleanLeft.value) return@f BooleanType.TRUE.toNormal()
            val right = yieldAll(other.evaluateValue())
                .orReturn { return@f it }
                .requireToBe<BooleanType> { return@f it }
            return@f right.toNormal()
        }
        BinaryOpType.COALESCE -> return@f (
            if (left == NullType) yieldAll(other.evaluateValue())
            else left.toNormal()
        )
        else -> {}
    }

    val right = yieldAll(other.evaluateValue())
        .orReturn { return@f it }

    when (operation) {
        BinaryOpType.LT -> return@f left.lessThan(right)
        BinaryOpType.GT -> return@f right.lessThan(left)
        BinaryOpType.LT_EQ -> {
            val greater = right.lessThan(left)
                .orReturn { return@f it }
            return@f (!greater).toNormal()
        }
        BinaryOpType.GT_EQ -> {
            val less = left.lessThan(right)
                .orReturn { return@f it }
            return@f (!less).toNormal()
        }
        BinaryOpType.PLUS -> {
            val leftAsString = left as? StringType
            val rightAsString = right as? StringType
            if (leftAsString != null || rightAsString != null) {
                val stringLeft = leftAsString
                    ?: stringify(left)
                        .orReturn { return@f it }
                val stringRight = rightAsString
                    ?: stringify(right)
                        .orReturn { return@f it }
                return@f (stringLeft + stringRight).toNormal()
            }
            // numeric values will be handled below
        }
        BinaryOpType.EQ ->
            return@f equal(left, right)
                .languageValue
                .toNormal()
        BinaryOpType.NOT_EQ ->
            return@f not { equal(left, right) }
                .languageValue
                .toNormal()
        BinaryOpType.IN -> {
            val key = left.requireToBeLanguageTypePropertyKey { return@f it }
            return@f right.hasProperty(key)
        }
        BinaryOpType.INSTANCEOF -> return@f when {
            right !is ClassType -> throwError(TypeErrorKind.INSTANCEOF_RHS_IS_NOT_CLASS)
            // there is no @@hasInstance
            left !is ObjectType -> BooleanType.FALSE.toNormal()
            else -> left.isInstanceOf(right)
                .languageValue
                .toNormal()
        }
        else -> {}
    }

    val numericLeft = left.requireToBe<NumericType<*>> { return@f it }
    val numericRight = right.requireToBe<NumericType<*>> { return@f it }
    if (left::class != right::class) return@f throwError(TypeErrorKind.BIGINT_MIXED_TYPES)

    return@f (
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
