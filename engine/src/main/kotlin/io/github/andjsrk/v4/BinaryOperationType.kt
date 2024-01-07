package io.github.andjsrk.v4

import io.github.andjsrk.v4.tokenize.TokenType

enum class BinaryOperationType {
    ASSIGN,
    // NOTE: operations without assignment are required right next to complex assignment operations
    // example: ASSIGN_A, ASSIGN_B, A, B, C
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
    THEN,
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
         * WARNING: The function assumes that a [BinaryOperationType] that has same name with [tokenType] exists;
         * otherwise the function will throw an exception.
         *
         * @throws IllegalArgumentException
         */
        fun fromTokenType(tokenType: TokenType) =
            BinaryOperationType.valueOf(tokenType.name)
        private val items by lazy { values() }
    }
}
