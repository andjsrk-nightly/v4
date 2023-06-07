package io.github.andjsrk.v4.tokenize

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.ErrorKind
import io.github.andjsrk.v4.error.SyntaxErrorKind
import io.github.andjsrk.v4.tokenize.TokenType.*

private const val HEX_ESCAPE_DIGIT_COUNT = 2

internal class Tokenizer(sourceText: String) {
    internal inner class CheckPoint {
        private val error = this@Tokenizer.error
        private val sourceCheckPoint = source.CheckPoint()
        fun load() {
            this@Tokenizer.error = error
            builder = Token.Builder()
            sourceCheckPoint.load()
        }
    }
    private val source = Source(sourceText)
    var error: Error? = null
        private set
    val hasError get() =
        error != null
    private fun reportError(kind: ErrorKind, range: Range = Range.since(source.pos, 1)): Boolean {
        if (!hasError) error = Error(kind, range)
        return false
    }
    // for use by methods
    private lateinit var builder: Token.Builder
    private fun advance(addRawContent: Boolean = true) =
        source.advance().also {
            if (addRawContent) builder.rawContent += it
        }
    private fun advanceWhile(addRawContent: Boolean, check: (Char) -> Boolean): Char {
        while (check(curr)) advance(addRawContent)
        return curr
    }
    private fun advanceWhile(check: (Char) -> Boolean) =
        advanceWhile(true, check)
    private lateinit var previousStateCheckPoint: CheckPoint
    /**
     * The tokenizer gets back to previous state.
     * Note that this function will not work correctly if try to call again before next token is computed,
     * because to support it [CheckPoint] needs to store every [Tokenizer]'s state so far.
     */
    fun back() {
        previousStateCheckPoint.load()
    }
    private val curr get() =
        source.curr
    private fun peek(relativePos: Int = 1) =
        source.peek(relativePos)
    private fun Token.Builder.addLiteralAdvance(char: Char = curr) {
        literal += char
        advance()
    }
    private fun Token.Builder.select(expected: Char, then: TokenType, `else`: TokenType): Token {
        advance()
        return (
            if (curr == expected) {
                advance()
                build(then)
            }
            else build(`else`)
        )
    }
    private fun Token.Builder.selectIf(expected: Char, then: TokenType) =
        select(expected, then, type)
    private fun takeCrLfNormalizedLineTerminator(): Char {
        require(curr.isLineTerminator)

        return (
            if (curr == '\r' && peek() == '\n') {
                advance()
                advance()
                '\n'
            } else curr.also { advance() }
        )
    }
    private val singleCharTokenMap =
        TokenType.values()
            .filter { it.staticContent?.length == 1 }
            .associateBy { it.staticContent!!.single() }
    private fun skipWhiteSpaceOrLineTerminator() {
        advanceWhile(false) {
            val isSpecLineTerminator = it.isLineTerminator
            if (isSpecLineTerminator) builder.isPrevLineTerminator = true
            it.isWhiteSpace || isSpecLineTerminator
        }
    }
    private fun skipSingleLineComment() {
        advanceWhile { it.not { isLineTerminator } }
    }
    private fun readHexDigits(length: Int): Pair<String, WasSuccessful> {
        var res = ""
        repeat(length) {
            if (curr.not { isHexDigit }) return res to false
            res += curr
            advance()
        }
        return res to true
    }
    private fun Token.Builder.addUnescapedHex4DigitsUnicodeEscapeSequence(beginPos: Int): WasSuccessful {
        val (stringMv, successful) = readHexDigits(4)
        val digitCount = stringMv.length
        val mv = stringMv.toHexIntOrNull()
        if (!successful || mv!! > MAX_CODE_POINT) return reportError(
            SyntaxErrorKind.INVALID_UNICODE_ESCAPE_SEQUENCE,
            Range.since(beginPos, digitCount + unicodeEscapeSequencePrefix.length),
        )
        literal += mv.toChar()
        return true
    }
    private fun Token.Builder.addUnescapedBraceHexDigitsUnicodeEscapeSequence(beginPos: Int): WasSuccessful {
        // { have already been read
        var hexDigits = ""
        advanceWhile {
            it.isHexDigit.thenAlso {
                hexDigits += it
            }
        }
        val mv = hexDigits.toHexIntOrNull()
        if (mv == null || mv > MAX_CODE_POINT || curr != '}') return reportError(
            SyntaxErrorKind.INVALID_UNICODE_ESCAPE_SEQUENCE,
            Range(beginPos, source.pos),
        )
        advance()
        literal += mv.toChar()
        return true
    }
    private fun Token.Builder.addUnescapedEscapeSequence(): WasSuccessful {
        // \ have already been read
        if (curr.isEndOfInput) return false

        when (curr) {
            in singleEscapeCharacterMap -> addLiteralAdvance(singleEscapeCharacterMap[curr]!!)
            'u' -> {
                val begin = source.pos - unicodeEscapeSequencePrefix.length
                advance()
                if (curr == '{') {
                    advance()
                    addUnescapedBraceHexDigitsUnicodeEscapeSequence(begin)
                } else addUnescapedHex4DigitsUnicodeEscapeSequence(begin)
            }
            'x' -> {
                val begin = source.pos - hexEscapeSequencePrefix.length
                advance()
                val charCode = readHexDigits(HEX_ESCAPE_DIGIT_COUNT)
                    .takeIf { (_, successful) -> successful }
                    ?.let { (it) -> it.toHexInt() }
                    ?: return reportError(
                        SyntaxErrorKind.INVALID_HEX_ESCAPE_SEQUENCE,
                        Range(begin, source.pos),
                    )
                literal += charCode.toChar()
            }
            else -> literal += curr
        }
        return true
    }
    private fun getStringToken(): Token {
        val quote = curr

        advance()

        builder.run {
            while (true) {
                advanceWhile {
                    (it != quote && it != '\\').thenAlso {
                        literal += it
                    }
                }

                while (curr == '\\') {
                    advance()
                    if (curr.isEndOfInput) return buildIllegal()
                    val successful = addUnescapedEscapeSequence()
                    if (!successful) return buildIllegal()
                }

                if (curr == quote) {
                    advance()
                    return build(STRING)
                }

                if (curr.isEndOfInput || curr.isLineTerminator) return buildIllegal()

                addLiteralAdvance()
            }
        }
    }
    private fun getTemplateHeadToken(): Token {
        // ` have already been read
        builder.run {
            while (true) {
                if (curr.isEndOfInput) return buildIllegal()

                when (curr) {
                    '`' -> {
                        advance()
                        return build(TEMPLATE_FULL)
                    }
                    '\\' -> {
                        advance()
                        addUnescapedEscapeSequence()
                        continue
                    }
                    else -> {
                        if (curr == '$' && peek() == '{') {
                            advance()
                            advance()
                            return build()
                        }
                        if (curr.isLineTerminator) {
                            literal += takeCrLfNormalizedLineTerminator()
                            continue
                        }
                    }
                }
                addLiteralAdvance()
            }
        }
    }
    /**
     * Note that this function will be called by parser because parser has a context about what current token should be.
     */
    fun getTemplateMiddleToken(): Token {
        advance() // read }
        builder.run {
            while (true) {
                if (curr.isEndOfInput) return buildIllegal()

                when (curr) {
                    '`' -> {
                        advance()
                        return build(TEMPLATE_TAIL)
                    }
                    '\\' -> {
                        advance()
                        addUnescapedEscapeSequence()
                        continue
                    }
                    else -> {
                        if (curr == '$' && peek() == '{') {
                            advance()
                            advance()
                            return build(TEMPLATE_MIDDLE)
                        }
                        if (curr.isLineTerminator) {
                            literal += takeCrLfNormalizedLineTerminator()
                            continue
                        }
                    }
                }
                addLiteralAdvance()
            }
        }
    }
    private fun Token.Builder.addDigitsWithNumericSeparators(check: (Char) -> Boolean, checkFirst: Boolean = true): WasSuccessful {
        if (checkFirst && !check(curr)) return false

        var separatorSeen = false

        while (check(curr) || curr.isNumericLiteralSeparator) {
            if (curr.isNumericLiteralSeparator) {
                advance()
                if (curr.isNumericLiteralSeparator) return reportError(SyntaxErrorKind.CONTINUOUS_NUMERIC_SEPARATOR)
                separatorSeen = true
                continue
            }
            separatorSeen = false
            addLiteralAdvance()
        }

        if (separatorSeen) return reportError(SyntaxErrorKind.TRAILING_NUMERIC_SEPARATOR)

        return true
    }
    private fun getNumberToken(): Token {
        builder.run {
            var seenPeriod = false
            val kind =
                if (curr == '0') {
                    addLiteralAdvance()
                    when (val lowercasePrev = curr.lowercaseChar()) {
                        'b', 'o', 'x' -> {
                            addLiteralAdvance()
                            NumberKind.getByKindSpecifier(lowercasePrev)!!
                        }
                        else -> NumberKind.DECIMAL
                    }
                } else NumberKind.DECIMAL
            val added = when (kind) {
                NumberKind.BINARY -> addDigitsWithNumericSeparators(Char::isBinaryDigit)
                NumberKind.OCTAL -> addDigitsWithNumericSeparators(Char::isOctalDigit)
                NumberKind.HEX -> addDigitsWithNumericSeparators(Char::isHexDigit)
                NumberKind.DECIMAL -> addDigitsWithNumericSeparators(Char::isDecimalDigit, false).also {
                    if (curr == '.') {
                        seenPeriod = true
                        val peeked = peek()
                        when {
                            peeked.isNumericLiteralSeparator -> {
                                addLiteralAdvance()
                                return buildIllegal()
                            }
                            peeked.isDecimalDigit -> {
                                addLiteralAdvance()
                                val added = addDigitsWithNumericSeparators(Char::isDecimalDigit)
                                if (!added) return buildIllegal()
                            }
                            else -> return build(NUMBER)
                        }
                    }
                }
            }
            if (!added) return buildIllegal()

            var isBigint = false
            if (curr == 'n' && !seenPeriod) {
                isBigint = true
                advance()
            } else if (curr.isExponentIndicator) {
                if (kind != NumberKind.DECIMAL) return buildIllegal()
                addLiteralAdvance()
                if (curr == '+' || curr == '-') addLiteralAdvance()
                if (curr.not { isDecimalDigit }) return buildIllegal()
                val successful = addDigitsWithNumericSeparators(Char::isDecimalDigit)
                if (!successful) return buildIllegal()
            }

            if (curr.isDecimalDigit || curr.isIdentifierChar) return buildIllegal()

            return build(if (isBigint) BIGINT else NUMBER)
        }
    }
    fun getNextToken(): Token {
        builder = Token.Builder()
        previousStateCheckPoint = CheckPoint()
        builder.run {
            do {
                startPos = source.pos
                type = when (curr) {
                    '#' -> PRIVATE_NAME
                    '"', '\'' -> STRING
                    '`' -> TEMPLATE_HEAD
                    else -> singleCharTokenMap[curr] ?: when {
                        curr.isWhiteSpace || curr.isLineTerminator -> WHITE_SPACE
                        curr.isDecimalDigit -> NUMBER
                        curr.isIdentifierChar -> IDENTIFIER
                        else -> ILLEGAL
                    }
                }

                when (type) {
                    COLON, SEMICOLON, COMMA, BITWISE_NOT,
                    LEFT_PARENTHESIS, LEFT_BRACE, LEFT_BRACKET,
                    RIGHT_PARENTHESIS, RIGHT_BRACE, RIGHT_BRACKET -> {
                        advance()
                        return build()
                    }
                    CONDITIONAL -> {
                        // ? ?. ?? ??=
                        advance()
                        return when (curr) {
                            '.' -> {
                                advance()
                                build(QUESTION_DOT)
                            }
                            '?' -> select('=', ASSIGN_COALESCE, COALESCE)
                            else -> build()
                        }
                    }
                    STRING -> return getStringToken()
                    LT -> {
                        // < <= << <<=
                        advance()
                        return when (curr) {
                            '=' -> {
                                advance()
                                build(LT_EQ)
                            }
                            '<' -> select('=', ASSIGN_SHL, SHL)
                            else -> build()
                        }
                    }
                    GT -> {
                        // > >= >> >>= >>> >>>=
                        advance()
                        return when (curr) {
                            '=' -> {
                                advance()
                                build(GT_EQ)
                            }
                            '>' -> {
                                advance()
                                when (curr) {
                                    '=' -> {
                                        advance()
                                        build(ASSIGN_SAR)
                                    }
                                    '>' -> select('=', ASSIGN_SHR, SHR)
                                    else -> build(SAR)
                                }
                            }
                            else -> build()
                        }
                    }
                    ASSIGN -> {
                        // = == =>
                        advance()
                        return when (curr) {
                            '=' -> {
                                advance()
                                build(EQ)
                            }
                            '>' -> {
                                advance()
                                build(ARROW)
                            }
                            else -> build()
                        }
                    }
                    NOT -> {
                        // ! !==
                        advance()
                        return if (curr == '=' && peek() == '=') {
                            advance()
                            advance()
                            build(NOT_EQ)
                        } else build()
                    }
                    PLUS -> {
                        // + += ++
                        advance()
                        return when (curr) {
                            '=' -> {
                                advance()
                                build(ASSIGN_PLUS)
                            }
                            '+' -> {
                                advance()
                                build(INCREMENT)
                            }
                            else -> build()
                        }
                    }
                    MINUS -> {
                        // - -= --
                        advance()
                        return when (curr) {
                            '=' -> {
                                advance()
                                build(ASSIGN_MINUS)
                            }
                            '-' -> {
                                advance()
                                build(DECREMENT)
                            }
                            else -> build()
                        }
                    }
                    MULTIPLY -> {
                        // * *= ** **=
                        advance()
                        return when (curr) {
                            '*' -> select('=', ASSIGN_EXPONENTIAL, EXPONENTIAL)
                            '=' -> build(ASSIGN_MULTIPLY)
                            else -> build()
                        }
                    }
                    // % %=
                    MOD -> return selectIf('=', ASSIGN_MOD)
                    DIVIDE -> {
                        // / /* /= //
                        advance()
                        when (curr) {
                            '/' -> skipSingleLineComment()
                            // '*' -> {
                            //     skipMultiLineComment()
                            //     continue
                            // }
                            '=' -> {
                                advance()
                                return build(ASSIGN_DIVIDE)
                            }
                            else -> return build()
                        }
                    }
                    BITWISE_AND -> {
                        // & &= && &&=
                        advance()
                        return when (curr) {
                            '&' -> select('=', ASSIGN_AND, AND)
                            '=' -> {
                                advance()
                                build(ASSIGN_BITWISE_AND)
                            }
                            else -> build()
                        }
                    }
                    BITWISE_OR -> {
                        // | |= || ||=
                        advance()
                        return when (curr) {
                            '|' -> select('=', ASSIGN_OR, OR)
                            '=' -> {
                                advance()
                                build(ASSIGN_BITWISE_OR)
                            }
                            else -> build()
                        }
                    }
                    // ^ ^=
                    BITWISE_XOR -> return selectIf('=', ASSIGN_BITWISE_XOR)
                    DOT -> {
                        // . ...
                        advance()
                        return if (curr == '.' && peek() == '.') {
                            advance()
                            advance()
                            build(ELLIPSIS)
                        } else build(DOT)
                    }
                    TEMPLATE_HEAD -> {
                        advance()
                        return getTemplateHeadToken()
                    }
                    PRIVATE_NAME -> TODO()
                    IDENTIFIER -> {
                        advanceWhile { it.isIdentifierChar }
                        return build(IDENTIFIER)
                    }
                    NUMBER -> return getNumberToken()
                    WHITE_SPACE -> skipWhiteSpaceOrLineTerminator()
                    ILLEGAL -> return (
                        if (curr.isEndOfInput) return build(if (hasError) ILLEGAL else EOS)
                        else {
                            advance()
                            build()
                        }
                    )
                    else -> TODO()
                }
            } while (builder.type == WHITE_SPACE)
        }
        return builder.build()
    }
}

private const val unicodeEscapeSequencePrefix = "\\u"
private const val hexEscapeSequencePrefix = "\\x"

private val Char.isEndOfInput get() =
    this == Source.endOfInput
