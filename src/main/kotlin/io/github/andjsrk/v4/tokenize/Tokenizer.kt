package io.github.andjsrk.v4.tokenize

class Tokenizer(sourceText: String) {
    private val source = Source(sourceText)
    var error: TokenizerError? = null
    val hasError get() =
        error != null
    fun reportError(err: TokenizerError) {
        if (!hasError) error = err
    }
    private val singleCharTokenMap =
        TokenType.values()
            .filter { it.staticContent?.length == 1 }
            .associateBy { it.staticContent!!.single() }

    fun getNextToken(): Token {
        val builder = Token.Builder(source.pos)

        do {
            builder.type = when (source.curr) {
                '#' -> TokenType.PRIVATE_NAME
                '"', '\'' -> TokenType.STRING
                '`' -> TokenType.TEMPLATE_HEAD
                else -> singleCharTokenMap[source.curr] ?: TokenType.UNINITIALIZED
            }

            when (builder.type) {
                TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN,
                TokenType.LEFT_BRACE, TokenType.RIGHT_BRACE,
                TokenType.LEFT_BRACK, TokenType.RIGHT_BRACK,
                TokenType.COLON, TokenType.SEMICOLON, TokenType.COMMA, TokenType.BIT_NOT, TokenType.ILLEGAL -> {
                    source.advance()
                    return builder.build()
                }
                TokenType.CONDITIONAL -> {
                    source.advance()

                }
            }
        }
    }
}
