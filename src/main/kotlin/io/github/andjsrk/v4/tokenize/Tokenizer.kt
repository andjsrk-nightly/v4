package io.github.andjsrk.v4.tokenize

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.Error
import io.github.andjsrk.v4.error.RangeError
import io.github.andjsrk.v4.error.SyntaxError
import io.github.andjsrk.v4.tokenize.TokenType.*

private typealias Length = Int

class Tokenizer(sourceText: String) {
    private val source = Source(sourceText)
    var error: ErrorWithRange? = null
        private set
    val hasError get() =
        error != null
    private fun reportError(kind: Error, range: Range = Range.since(source.pos, 1)): Boolean {
        if (!hasError) error = ErrorWithRange(kind, range)
        return false
    }
    // for use by methods
    private lateinit var builder: Token.Builder
    /**
     * If `true`, when got `` ` `` it should be interpreted as start of `TEMPLATE_HEAD`.
     * If `false`, when got `}` it should be interpreted as start of `TEMPLATE_MIDDLE` or `TEMPLATE_TAIL`.
     */
    private val syntacticPairs = ArrayDeque<SyntacticPair>()
    private fun ArrayDeque<SyntacticPair>.isLastClosingPart(value: Char) =
        lastOrNull()?.closingPart == value.toString()
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
        require(curr.isSpecLineTerminator)

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
            val isSpecLineTerminator = it.isSpecLineTerminator
            if (isSpecLineTerminator) builder.afterLineTerminator = true
            it.isSpecWhiteSpace || isSpecLineTerminator
        }
    }
    private fun skipSingleLineComment() {
        advanceWhile { it.isSpecLineTerminator.not() }
    }
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
        if (mv == null || mv > specMaxCodePoint) return reportError(
            SyntaxError.INVALID_UNICODE_ESCAPE_SEQUENCE,
            Range.since(beginPos, digitCount + unicodeEscapeSequencePrefix.length),
        )
        literal += mv.toChar()
        return true
    }
    private fun Token.Builder.addUnescapedBraceHexDigitsUnicodeEscapeSequence(beginPos: Int): WasSuccessful {
        // { have already been read
        var hexDigits = ""
        advanceWhile {
            it.isSpecHexDigit.thenAlso {
                hexDigits += it
            }
        }
        val mv = hexDigits.toHexIntOrNull()
        if (mv == null || mv > specMaxCodePoint || curr != '}') return reportError(
            SyntaxError.INVALID_UNICODE_ESCAPE_SEQUENCE,
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
            in specSingleEscapeCharacterMap -> addLiteralAdvance(specSingleEscapeCharacterMap[curr]!!)
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
                val (_, charCode) = readHexIntOrNull(2)
                if (charCode == null) return reportError(
                    SyntaxError.INVALID_HEX_ESCAPE_SEQUENCE,
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

                if (curr.isEndOfInput || curr.isSpecLineTerminator) return buildIllegal()

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
                            syntacticPairs.add(SyntacticPair.TEMPLATE_LITERAL)
                            return build()
                        }
                        if (curr.isSpecLineTerminator) {
                            literal += takeCrLfNormalizedLineTerminator()
                            continue
                        }
                    }
                }
                addLiteralAdvance()
            }
        }
    }
    private fun getTemplateMiddleToken(): Token {
        // } have already been read
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
                            syntacticPairs.add(SyntacticPair.TEMPLATE_LITERAL)
                            return build(TEMPLATE_MIDDLE)
                        }
                        if (curr.isSpecLineTerminator) {
                            literal += takeCrLfNormalizedLineTerminator()
                            continue
                        }
                    }
                }
                addLiteralAdvance()
            }
        }
    }
    private fun Token.Builder.addDigitsWithNumericSeparators(check: (Char) -> Boolean): WasSuccessful {
        if (!check(curr)) return false

        var separatorSeen = false

        while (check(curr) || curr.isSpecNumericLiteralSeparator) {
            if (curr.isSpecNumericLiteralSeparator) {
                advance()
                if (curr.isSpecNumericLiteralSeparator) return reportError(RangeError.CONTINUOUS_NUMERIC_SEPARATOR)
                separatorSeen = true
                continue
            }
            separatorSeen = false
            addLiteralAdvance()
        }

        if (separatorSeen) return reportError(RangeError.TRAILING_NUMERIC_SEPARATOR)

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
                NumberKind.BINARY -> addDigitsWithNumericSeparators(Char::isSpecBinaryDigit)
                NumberKind.OCTAL -> addDigitsWithNumericSeparators(Char::isSpecOctalDigit)
                NumberKind.HEX -> addDigitsWithNumericSeparators(Char::isSpecHexDigit)
                NumberKind.DECIMAL -> addDigitsWithNumericSeparators(Char::isSpecDecimalDigit).also {
                    if (curr == '.') {
                        seenPeriod = true
                        val peeked = peek()
                        when {
                            peeked.isSpecNumericLiteralSeparator -> {
                                addLiteralAdvance()
                                return buildIllegal()
                            }
                            peeked.isSpecDecimalDigit -> {
                                addLiteralAdvance()
                                val added = addDigitsWithNumericSeparators(Char::isSpecDecimalDigit)
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
            } else if (curr.isSpecExponentIndicator) {
                if (kind != NumberKind.DECIMAL) return buildIllegal()
                addLiteralAdvance()
                if (curr == '+' || curr == '-') addLiteralAdvance()
                if (curr.isSpecDecimalDigit.not()) return buildIllegal()
                val successful = addDigitsWithNumericSeparators(Char::isSpecDecimalDigit)
                if (!successful) return buildIllegal()
            }

            if (curr.isSpecDecimalDigit || curr.isSpecIdentifierName) return buildIllegal()

            return build(if (isBigint) BIGINT else NUMBER)
        }
    }
    fun getNextToken(): Token {
        builder = Token.Builder()
        builder.run {
            do {
                startPos = source.pos
                type = when (curr) {
                    '#' -> PRIVATE_NAME
                    '"', '\'' -> STRING
                    '`' -> TEMPLATE_HEAD
                    else -> singleCharTokenMap[curr] ?: when {
                        curr.isSpecWhiteSpace || curr.isSpecLineTerminator -> WHITE_SPACE
                        curr.isSpecDecimalDigit -> NUMBER
                        curr.isSpecIdentifierName -> IDENTIFIER
                        else -> ILLEGAL
                    }
                }

                when (type) {
                    COLON, SEMICOLON, COMMA, BITWISE_NOT -> {
                        advance()
                        return build()
                    }
                    LEFT_PARENTHESIS, LEFT_BRACE, LEFT_BRACKET -> {
                        syntacticPairs.add(SyntacticPair.findByOpeningPart(curr.toString())!!)
                        advance()
                        return build()
                    }
                    RIGHT_PARENTHESIS, RIGHT_BRACKET -> return (
                        if (syntacticPairs.isLastClosingPart(curr)) {
                            syntacticPairs.removeLast()
                            advance()
                            build()
                        } else {
                            reportError(SyntaxError.UNEXPECTED_TOKEN, Range.since(source.pos, 1))
                            advance()
                            buildIllegal()
                        }
                    )
                    RIGHT_BRACE -> return when (syntacticPairs.lastOrNull()) {
                        SyntacticPair.TEMPLATE_LITERAL -> {
                            syntacticPairs.removeLast()
                            advance()
                            getTemplateMiddleToken()
                        }
                        SyntacticPair.BRACE -> {
                            syntacticPairs.removeLast()
                            advance()
                            build()
                        }
                        else -> {
                            reportError(SyntaxError.UNEXPECTED_TOKEN, Range.since(source.pos, 1))
                            advance()
                            buildIllegal()
                        }
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
                                    else -> build()
                                }
                            }
                            else -> build()
                        }
                    }
                    ASSIGN -> {
                        // = == === =>
                        advance()
                        return when (curr) {
                            '=' -> select('=', EQ_STRICT, EQ)
                            '>' -> {
                                advance()
                                build(ARROW)
                            }
                            else -> build()
                        }
                    }
                    NOT -> {
                        // ! != !==
                        advance()
                        return when (curr) {
                            '=' -> select('=', NOT_EQ_STRICT, NOT_EQ)
                            else -> build(NOT)
                        }
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
                        advanceWhile { it.isSpecIdentifierName }
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
