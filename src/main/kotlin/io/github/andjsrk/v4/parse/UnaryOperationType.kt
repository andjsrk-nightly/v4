package io.github.andjsrk.v4.parse

enum class UnaryOperationType {
    MINUS,
    NOT,
    BITWISE_NOT,
    INCREMENT,
    DECREMENT,

    // keywords
    AWAIT,
    TYPEOF,
    VOID;

    val isUpdate get() =
        this == INCREMENT || this == DECREMENT
}
