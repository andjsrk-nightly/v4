package io.github.andjsrk.v4.tokenize

/**
 * @param staticContent A static content of the token.
 * `null` if the token contains dynamic content.
 */
enum class TokenType(val staticContent: String?) {
    EOS(null),
    ILLEGAL(null),
    TEMPLATE_FULL(null),
    TEMPLATE_HEAD(null),
    TEMPLATE_MIDDLE(null),
    TEMPLATE_TAIL(null),
    COMMA(","),
    DOT("."),
    LEFT_BRACKET("["),
    QUESTION_DOT("?."),
    LEFT_PARENTHESIS("("),
    RIGHT_PARENTHESIS(")"),
    RIGHT_BRACKET("]"),
    LEFT_BRACE("{"),
    COLON(":"),
    ELLIPSIS("..."),
    QUESTION("?"),
    SEMICOLON(";"),
    RIGHT_BRACE("}"),
    ARROW("=>"),
    ASSIGN("="),
    ASSIGN_COALESCE("??="),
    ASSIGN_OR("||="),
    ASSIGN_AND("&&="),
    ASSIGN_BITWISE_OR("|="),
    ASSIGN_BITWISE_XOR("^="),
    ASSIGN_BITWISE_AND("&="),
    ASSIGN_SHL("<<="),
    ASSIGN_SAR(">>="),
    ASSIGN_SHR(">>>="),
    ASSIGN_MULTIPLY("*="),
    ASSIGN_DIVIDE("/="),
    ASSIGN_MOD("%="),
    ASSIGN_EXPONENTIAL("**="),
    ASSIGN_PLUS("+="),
    ASSIGN_MINUS("-="),
    COALESCE("??"),
    OR("||"),
    AND("&&"),
    BITWISE_OR("|"),
    BITWISE_XOR("^"),
    BITWISE_AND("&"),
    SHL("<<"),
    SAR(">>"),
    SHR(">>>"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MOD("%"),
    EXPONENTIAL("**"),
    PLUS("+"),
    MINUS("-"),
    THEN("&&>"),
    NOT("!"),
    BITWISE_NOT("~"),
    INCREMENT("++"),
    DECREMENT("--"),
    EQ("=="),
    NOT_EQ("!=="),
    LT("<"),
    GT(">"),
    LT_EQ("<="),
    GT_EQ(">="),
    NUMBER(null),
    BIGINT(null),
    STRING(null),
    PRIVATE_NAME(null),
    WHITE_SPACE(null),
    REGEXP_LITERAL(null),
    IDENTIFIER(null);

    val isAssignLike by lazy {
        this in ASSIGN..ASSIGN_MINUS
    }
    val isTemplateStart by lazy {
        this in TEMPLATE_FULL..TEMPLATE_HEAD
    }
}
