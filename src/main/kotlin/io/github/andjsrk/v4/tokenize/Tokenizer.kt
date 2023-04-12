package io.github.andjsrk.v4.tokenize

import io.github.andjsrk.v4.*

private typealias WasSuccessful = Boolean
private typealias Length = Int

class Tokenizer(sourceText: String) {
    private val source = Source(sourceText)
    var error: TokenizerError? = null
    val hasError get() =
        error != null
    private fun reportError(err: TokenizerError) {
        if (!hasError) error = err
    }
    private fun advance() =
        source.advance()
    private fun <T> T.alsoAdvance() =
        this.also { advance() }
    private fun advanceWhile(check: (Char) -> Boolean) =
        source.advanceWhile(check)
    private val curr get() =
        source.curr
    private fun peek(relativePos: Int = 1) =
        source.peek(relativePos)
    private fun Token.Builder.select(expected: Char, then: TokenType, `else`: TokenType): Token {
        advance()
        return (
            if (curr == expected) build(then).alsoAdvance()
            else build(`else`)
        )
    }
    private val singleCharTokenMap =
        TokenType.values()
            .filter { it.staticContent?.length == 1 }
            .associateBy { it.staticContent!!.single() }
    private fun readHexDigits(maxLength: Int): String {
        var res = ""
        repeat(maxLength) {
            if (curr.isSpecHexDigit.not()) return res
            res += curr
            advance()
        }
        return res
    }
    private fun readHexIntOrNull(length: Int): Pair<Length, Int?> {
        val digits = readHexDigits(length)
        return digits.length to (
            if (digits.length != length) null
            else digits.toHexIntOrNull()
        )
    }
    private fun Token.Builder.addUnescapedHex4DigitsUnicodeEscapeSequence(beginPos: Int): WasSuccessful {
        val (digitCount, mv) = readHexIntOrNull(4)
        if (mv == null || mv > specMaxCodePoint) {
            reportError(
                TokenizerError(
                    SyntaxError.INVALID_UNICODE_ESCAPE_SEQUENCE,
                    Location.since(beginPos, digitCount + unicodeEscapeSequencePrefix.length)
                )
            )
            return false
        }
        literal += mv.toChar()
        return true
    }
    private fun Token.Builder.addUnescapedBraceHexDigitsUnicodeEscapeSequence(beginPos: Int): WasSuccessful {
        // { have already been read
        TODO("there's a bug")
        var hexDigits = ""
        advanceWhile {
            it.isSpecHexDigit.also { isHexDigit ->
                if (isHexDigit) hexDigits += it
            }
        }
        val mv = hexDigits.toHexIntOrNull()
        if (mv == null || mv > specMaxCodePoint) {
            reportError(
                TokenizerError(
                    SyntaxError.INVALID_UNICODE_ESCAPE_SEQUENCE,
                    Location.since(beginPos, hexDigits.length)
                )
            )
            return false
        }
        literal += mv.toChar()
        return true
    }
    private fun Token.Builder.addUnescapedEscapeSequence(): WasSuccessful {
        if (curr.isEndOfInput) return false

        when (curr) {
            in specSingleEscapeCharacterMap -> {
                literal += specSingleEscapeCharacterMap[curr]
                advance()
            }
            'u' -> {
                val begin = source.pos - unicodeEscapeSequencePrefix.length
                advance()
                if (curr == '{') {
                    advance()
                    addUnescapedBraceHexDigitsUnicodeEscapeSequence(begin)
                } else addUnescapedHex4DigitsUnicodeEscapeSequence(begin)
            }
            'x' -> {
                advance()
                val (_, charCode) = readHexIntOrNull(2)
                if (charCode == null) return false
                literal += charCode.toChar()
            }
            else -> {
                literal += curr
            }
        }
        return true
    }
    private fun getStringToken(builder: Token.Builder): Token {
        val quote = curr

        advance()

        while (true) {
            advanceWhile {
                (it != quote && it != '\\').also { shouldContinue ->
                    if (shouldContinue) builder.literal += it
                }
            }

            while (curr == '\\') {
                advance()
                if (curr.isEndOfInput) return builder.build(TokenType.ILLEGAL)
                val successful = builder.addUnescapedEscapeSequence()
                if (!successful) return builder.build(TokenType.ILLEGAL)
            }

            if (curr == quote) {
                advance()
                return builder.build(TokenType.STRING)
            }

            if (curr.isEndOfInput || curr.isSpecLineTerminator) return builder.build(TokenType.ILLEGAL)

            builder.literal += curr
        }
    }
    fun getNextToken(): Token {
        val builder = Token.Builder(source.pos)

        do {
            builder.type = when (curr) {
                '#' -> TokenType.PRIVATE_NAME
                '"', '\'' -> TokenType.STRING
                '`' -> TokenType.TEMPLATE_HEAD
                else -> singleCharTokenMap[curr] ?: TokenType.UNINITIALIZED
            }

            when (builder.type) {
                TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN,
                TokenType.LEFT_BRACE, TokenType.RIGHT_BRACE,
                TokenType.LEFT_BRACK, TokenType.RIGHT_BRACK,
                TokenType.COLON, TokenType.SEMICOLON, TokenType.COMMA, TokenType.BIT_NOT, TokenType.ILLEGAL ->
                    return builder.build().alsoAdvance()
                TokenType.CONDITIONAL -> {
                    advance()
                    return when (curr) {
                        '.' -> builder.build(TokenType.QUESTION_PERIOD).alsoAdvance()
                        '?' -> builder.select('=', TokenType.ASSIGN_NULLISH, TokenType.NULLISH)
                        else -> builder.build()
                    }
                }
                TokenType.STRING -> return getStringToken(builder)
                else -> return builder.build(TokenType.ILLEGAL)
            }
        } while (true)
    }
}

private const val unicodeEscapeSequencePrefix = "\\u"
private const val hexEscapeSequencePrefix = "\\x"

private val Char.isEndOfInput get() =
    this == Source.endOfInput
