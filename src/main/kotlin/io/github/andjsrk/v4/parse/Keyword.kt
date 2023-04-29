package io.github.andjsrk.v4.parse

enum class Keyword {
    ASYNC,
    AWAIT,
    BREAK,
    CASE,
    CATCH,
    CLASS,
    CONST,
    CONTINUE,
    DEFAULT,
    ELSE,
    EXPORT,
    EXTENDS,
    FALSE,
    FINALLY,
    FOR,
    IF,
    IMPORT,
    IN,
    INSTANCEOF,
    NEW,
    NULL,
    RETURN,
    SUPER,
    SWITCH,
    THIS,
    THROW,
    TRUE,
    TRY,
    TYPEOF,
    VOID,
    WHILE,
    YIELD;

    val value = name.lowercase()
}
