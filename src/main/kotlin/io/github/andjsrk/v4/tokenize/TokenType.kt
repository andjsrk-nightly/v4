package io.github.andjsrk.v4.tokenize

enum class TokenType(val staticContent: String?, val precedence: Int = 0) {
    UNINITIALIZED(null),
    ILLEGAL(null),
    TEMPLATE_HEAD(null),
    TEMPLATE_MIDDLE(null),
    TEMPLATE_TAIL(null),
    PERIOD("."),
    LEFT_BRACK("["),
    QUESTION_PERIOD("?."),
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),
    RIGHT_BRACK("]"),
    LEFT_BRACE("{"),
    COLON(":"),
    ELLIPSIS("..."),
    CONDITIONAL("?", 3),
    SEMICOLON(";"),
    RIGHT_BRACE("}"),
    ARROW("=>"),
    ASSIGN("=", 2),
    ASSIGN_NULLISH("??=", 2),
    ASSIGN_OR("||=", 2),
    ASSIGN_AND("&&=", 2),
    ASSIGN_BIT_OR("|=", 2),
    ASSIGN_BIT_XOR("^=", 2),
    ASSIGN_BIT_AND("&=", 2),
    ASSIGN_SHL("<<=", 2),
    ASSIGN_SAR(">>=", 2),
    ASSIGN_SHR(">>>=", 2),
    ASSIGN_MUL("*=", 2),
    ASSIGN_DIV("/=", 2),
    ASSIGN_MOD("%=", 2),
    ASSIGN_EXP("**=", 2),
    ASSIGN_ADD("+=", 2),
    ASSIGN_SUB("-=", 2),
    COMMA(",", 1),
    NULLISH("??", 3),
    OR("||", 4),
    AND("&&", 5),
    BIT_OR("|", 6),
    BIT_XOR("^", 7),
    BIT_AND("&", 8),
    SHL("<<", 11),
    SAR(">>", 11),
    SHR(">>>", 11),
    MUL("*", 13),
    DIV("/", 13),
    MOD("%", 13),
    EXPONENTIAL("**", 14),
    ADD("+", 12),
    SUBTRACT("-", 12),
    NOT("!"),
    BIT_NOT("~"),
    INC("++"),
    DEC("--"),
    EQ("==", 9),
    EQ_STRICT("===", 9),
    NOT_EQ("!=", 9),
    NOT_EQ_STRICT("!==", 9),
    LT("<", 10),
    GT(">", 10),
    LT_EQ("<=", 10),
    GT_EQ(">=", 10),
    NUMBER(null),
    SMI(null),
    BIGINT(null),
    STRING(null),
    FUTURE_STRICT_RESERVED_WORD(null),
    ESCAPED_STRICT_RESERVED_WORD(null),
    PRIVATE_NAME(null),
    ESCAPED_KEYWORD(null),
    WHITESPACE(null),
    REGEXP_LITERAL(null),
    IDENTIFIER(null),
    KEYWORD(null, 0);
}
