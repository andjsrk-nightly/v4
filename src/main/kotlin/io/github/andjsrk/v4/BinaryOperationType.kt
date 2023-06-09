package io.github.andjsrk.v4

import io.github.andjsrk.v4.tokenize.TokenType

enum class BinaryOperationType {
    ASSIGN,
    ASSIGN_COALESCE,
    ASSIGN_OR,
    ASSIGN_AND,
    ASSIGN_BITWISE_OR,
    ASSIGN_BITWISE_XOR,
    ASSIGN_BITWISE_AND,
    ASSIGN_SHL,
    ASSIGN_SAR,
    ASSIGN_SHR,
    ASSIGN_MULTIPLY,
    ASSIGN_DIVIDE,
    ASSIGN_MOD,
    ASSIGN_EXPONENTIAL,
    ASSIGN_PLUS,
    ASSIGN_MINUS,
    COALESCE,
    OR,
    THEN,
    AND,
    BITWISE_OR,
    BITWISE_XOR,
    BITWISE_AND,
    SHL,
    SAR,
    SHR,
    MULTIPLY,
    DIVIDE,
    MOD,
    EXPONENTIAL,
    PLUS,
    MINUS,
    EQ,
    NOT_EQ,
    LT,
    GT,
    LT_EQ,
    GT_EQ,

    // keywords
    INSTANCEOF,
    IN;

    val isAssignLike by lazy {
        this in ASSIGN..ASSIGN_MINUS
    }
    fun toNonAssign(): BinaryOperationType {
        require(this in ASSIGN_COALESCE..ASSIGN_MINUS)
        return items[ordinal + (COALESCE.ordinal - ASSIGN_COALESCE.ordinal)]
    }

    companion object {
        /**
         * Warning: call this function only if you are sure of the operation is included in this enum.
         */
        fun fromTokenType(tokenType: TokenType) =
            BinaryOperationType.valueOf(tokenType.name)
        private val items by lazy { values() }
    }
}
