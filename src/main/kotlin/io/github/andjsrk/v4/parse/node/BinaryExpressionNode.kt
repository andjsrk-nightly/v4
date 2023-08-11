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
    override fun evaluate() =
        EvalFlow {
            if (operation.isAssignLike) {
                // TODO: destructuring assignment

                val lref = left.evaluate()
                    .returnIfAbrupt(this) { return@EvalFlow }
                    as Reference
                val rval =
                    if (operation == BinaryOpType.ASSIGN) {
                        if (left is IdentifierNode && right.isAnonymous) right.evaluateWithName(left.stringValue)
                        else right.evaluateValue()
                            .returnIfAbrupt(this) { return@EvalFlow }
                    } else {
                        val lval = getValue(lref).returnIfAbrupt { return@EvalFlow }
                        lval.operate(operation.toNonAssign(), right)
                            .returnIfAbrupt(this) { return@EvalFlow }
                    }
                lref.putValue(rval)
                    .returnIfAbrupt { `return`(it) }
                `return`(rval.toNormal())
            }

            val lval = left.evaluateValue()
                .returnIfAbrupt(this) { return@EvalFlow }

            `return`(yieldAll(lval.operate(operation, right)))
        }
}

/**
 * Applies binary operator except assignments.
 *
 * Note that [other] is not a [LanguageType] because right side needs to be evaluated conditionally for some operations.
 */
internal fun LanguageType.operate(operation: BinaryOpType, other: ExpressionNode) =
    EvalFlow {
        assert(operation.not { isAssignLike })

        val left = this@operate

        when (operation) {
            BinaryOpType.AND -> {
                val booleanLeft = left.requireToBe<BooleanType> { `return`(it) }
                if (!booleanLeft.value) `return`(BooleanType.FALSE.toNormal())
                val right = other.evaluateValue()
                    .returnIfAbrupt(this) { return@EvalFlow }
                    .requireToBe<BooleanType> { `return`(it) }
                `return`(right.toNormal())
            }
            BinaryOpType.THEN -> {
                val booleanLeft = left.requireToBe<BooleanType> { `return`(it) }
                if (!booleanLeft.value) `return`(BooleanType.FALSE.toNormal())
                `return`(yieldAll(other.evaluateValue()))
            }
            BinaryOpType.OR -> {
                val booleanLeft = left.requireToBe<BooleanType> { `return`(it) }
                if (booleanLeft.value) `return`(BooleanType.TRUE.toNormal())
                val right = other.evaluateValue()
                    .returnIfAbrupt(this) { return@EvalFlow }
                    .requireToBe<BooleanType> { `return`(it) }
                `return`(right.toNormal())
            }
            BinaryOpType.COALESCE -> `return`(
                if (left == NullType) yieldAll(other.evaluateValue())
                else left.toNormal()
            )
            else -> {}
        }

        val right = other.evaluateValue()
            .returnIfAbrupt(this) { return@EvalFlow }

        when (operation) {
            BinaryOpType.LT, BinaryOpType.GT, BinaryOpType.LT_EQ, BinaryOpType.GT_EQ ->
                `return`(
                    if (left is StringType || left is NumericType<*>) {
                        if (left::class != right::class) throwError(TypeErrorKind.LHS_RHS_NOT_SAME_TYPE)
                        else Completion.Normal(
                            when (operation) {
                                BinaryOpType.LT -> left.isLessThan(right, BooleanType.FALSE)
                                BinaryOpType.GT -> right.isLessThan(left, BooleanType.FALSE)
                                BinaryOpType.LT_EQ -> !right.isLessThan(left, BooleanType.TRUE)
                                BinaryOpType.GT_EQ -> !left.isLessThan(right, BooleanType.TRUE)
                                else -> neverHappens()
                            }
                        )
                    }
                    else unexpectedType(left, StringType::class, NumericType::class)
                )
            BinaryOpType.PLUS -> {
                val leftAsString = left as? StringType
                val rightAsString = right as? StringType
                if (leftAsString != null || rightAsString != null) {
                    val stringLeft = leftAsString
                        ?: stringify(left)
                            .returnIfAbrupt { `return`(it) }
                    val stringRight = rightAsString
                        ?: stringify(right)
                            .returnIfAbrupt { `return`(it) }
                    `return`((stringLeft + stringRight).toNormal())
                }
                // numeric values will be handled on below
            }
            BinaryOpType.EQ -> `return`(equal(left, right).languageValue.toNormal())
            BinaryOpType.NOT_EQ -> `return`(not { equal(left, right) }.languageValue.toNormal())
            else -> {}
        }

        val numericLeft = left.requireToBe<NumericType<*>> { `return`(it) }
        val numericRight = right.requireToBe<NumericType<*>> { `return`(it) }
        if (left::class != right::class) returnError(TypeErrorKind.BIGINT_MIXED_TYPES)

        `return`(
            @CompilerFalsePositive
            @Suppress("TYPE_MISMATCH")
            when (operation) {
                BinaryOpType.EXPONENTIAL -> numericLeft.pow(numericRight)
                BinaryOpType.MULTIPLY -> numericLeft * numericRight
                BinaryOpType.DIVIDE -> numericLeft / numericRight
                BinaryOpType.MOD -> numericLeft % numericRight
                BinaryOpType.PLUS -> numericLeft + numericRight
                BinaryOpType.MINUS -> numericLeft - numericRight
                BinaryOpType.SHL -> numericLeft.leftShift(numericRight)
                BinaryOpType.SAR -> numericLeft.signedRightShift(numericRight)
                BinaryOpType.SHR -> numericLeft.unsignedRightShift(numericRight)
                BinaryOpType.BITWISE_AND -> numericLeft.bitwiseAnd(numericRight)
                BinaryOpType.BITWISE_XOR -> numericLeft.bitwiseXor(numericRight)
                BinaryOpType.BITWISE_OR -> numericLeft.bitwiseOr(numericRight)
                else -> missingBranch()
            }
                .extractFromCompletionOrReturn { `return`(it) }
                .toNormal()
        )
    }
