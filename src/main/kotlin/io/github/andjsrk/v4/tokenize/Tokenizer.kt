package io.github.andjsrk.v4.tokenize

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.Error
import io.github.andjsrk.v4.error.RangeError
import io.github.andjsrk.v4.error.SyntaxError
import io.github.andjsrk.v4.tokenize.TokenType.*

private typealias WasSuccessful = Boolean
private typealias Length = Int

class Tokenizer(sourceText: String) {
    private val source = Source(sourceText)
    var error: TokenizerError? = null
        private set
    val hasError get() =
        error != null
    private fun reportError(kind: Error, location: Location = Location.since(source.pos, 1)) {
        if (!hasError) error = TokenizerError(kind, location)
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
    private fun advance() =
        source.advance().also {
            builder.rawContent += it
        }
    private fun <T> T.alsoAdvance() =
        this.also { advance() }
    private fun advanceWhile(check: (Char) -> Boolean): Char {
        while (check(curr)) advance()
        return curr
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
        require(curr.isSpecLineTerminator)

        return (
            if (curr == '\r' && peek() == '\n') {
                advance()
                '\n'
            } else curr
        ).alsoAdvance()
    }
    private val singleCharTokenMap =
        TokenType.values()
            .filter { it.staticContent?.length == 1 }
            .associateBy { it.staticContent!!.single() }
    private fun skipWhiteSpaceOrLineTerminator() {
        run { // isolated scope
            val isSpecWhiteSpace = curr.isSpecWhiteSpace
            val isSpecLineTerminator = curr.isSpecLineTerminator
            if (isSpecWhiteSpace.not() && isSpecLineTerminator.not()) return

            if (isSpecLineTerminator) builder.afterLineTerminator = true
        }

        advanceWhile {
            val isSpecLineTerminator = it.isSpecLineTerminator
            (it.isSpecWhiteSpace || isSpecLineTerminator).thenAlso {
                if (isSpecLineTerminator) builder.afterLineTerminator = true
            }
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
        if (mv == null || mv > specMaxCodePoint) {
            reportError(
                SyntaxError.INVALID_UNICODE_ESCAPE_SEQUENCE,
                Location.since(beginPos, digitCount + unicodeEscapeSequencePrefix.length),
            )
            return false
        }
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
        if (mv == null || mv > specMaxCodePoint || curr != '}') {
            reportError(SyntaxError.INVALID_UNICODE_ESCAPE_SEQUENCE, Location(beginPos, source.pos))
            return false
        }
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
                if (charCode == null) {
                    reportError(SyntaxError.INVALID_HEX_ESCAPE_SEQUENCE, Location(begin, source.pos))
                    return false
                }
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
    private fun Token.Builder.addDigitsWithNumericSeparators(check: (Char) -> Boolean, checkFirstDigit: Boolean = true): WasSuccessful {
        if (checkFirstDigit && !check(curr)) return false

        var separatorSeen = false

        while (check(curr) || curr.isSpecNumericLiteralSeparator) {
            if (curr.isSpecNumericLiteralSeparator) {
                advance()
                if (curr.isSpecNumericLiteralSeparator) {
                    reportError(RangeError.CONTINUOUS_NUMERIC_SEPARATOR)
                    return false
                }
                separatorSeen = true
                continue
            }
            separatorSeen = false
            addLiteralAdvance()
        }

        if (separatorSeen) {
            reportError(RangeError.TRAILING_NUMERIC_SEPARATOR)
            return false
        }

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
            val successful = when (kind) {
                NumberKind.BINARY -> addDigitsWithNumericSeparators(Char::isSpecBinaryDigit)
                NumberKind.OCTAL -> addDigitsWithNumericSeparators(Char::isSpecOctalDigit)
                NumberKind.HEX -> addDigitsWithNumericSeparators(Char::isSpecHexDigit)
                NumberKind.DECIMAL -> addDigitsWithNumericSeparators(Char::isSpecDecimalDigit).also {
                    if (curr == '.') {
                        seenPeriod = true
                        addLiteralAdvance('.')
                        if (curr.isSpecNumericLiteralSeparator) return buildIllegal()
                        val successful = addDigitsWithNumericSeparators(Char::isSpecDecimalDigit)
                        if (!successful) return buildIllegal()
                    }
                }
            }
            if (!successful) return buildIllegal()

            var isBigint = false
            if (curr == 'n' && !seenPeriod) { // bigint literal
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
        builder = Token.Builder(source.pos)
        builder.run {
            do {
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
                    COLON, SEMICOLON, COMMA, BIT_NOT, ILLEGAL -> return build().alsoAdvance()
                    LEFT_PAREN, LEFT_BRACE, LEFT_BRACK -> {
                        syntacticPairs.add(SyntacticPair.findByOpeningPart(curr.toString())!!)
                        advance()
                        return build()
                    }
                    RIGHT_PAREN, RIGHT_BRACK -> return (
                        if (syntacticPairs.isLastClosingPart(curr)) {
                            syntacticPairs.removeLast()
                            advance()
                            build()
                        } else {
                            reportError(SyntaxError.UNEXPECTED_TOKEN, Location.since(source.pos, 1))
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
                            reportError(SyntaxError.UNEXPECTED_TOKEN, Location.since(source.pos, 1))
                            advance()
                            buildIllegal()
                        }
                    }
                    CONDITIONAL -> {
                        // ? ?. ?? ??=
                        advance()
                        return when (curr) {
                            '.' -> build(QUESTION_PERIOD).alsoAdvance()
                            '?' -> select('=', ASSIGN_NULLISH, NULLISH)
                            else -> build()
                        }
                    }
                    STRING -> return getStringToken()
                    LT -> {
                        // < <= << <<=
                        advance()
                        return when (curr) {
                            '=' -> build(LT_EQ).alsoAdvance()
                            '<' -> select('=', ASSIGN_SHL, SHL)
                            else -> build()
                        }
                    }
                    GT -> {
                        // > >= >> >>= >>> >>>=
                        advance()
                        return when (curr) {
                            '=' -> build(GT_EQ).alsoAdvance()
                            '>' -> {
                                advance()
                                when (curr) {
                                    '=' -> build(ASSIGN_SAR).alsoAdvance()
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
                            '>' -> build(ARROW).alsoAdvance()
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
                    ADD -> {
                        // + += ++
                        advance()
                        return when (curr) {
                            '=' -> build(ASSIGN_ADD).alsoAdvance()
                            '+' -> build(INC).alsoAdvance()
                            else -> build()
                        }
                    }
                    SUBTRACT -> {
                        // - -= --
                        advance()
                        return when (curr) {
                            '=' -> build(ASSIGN_SUBTRACT).alsoAdvance()
                            '-' -> build(DEC).alsoAdvance()
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
                        return when (curr) {
                            '/' -> {
                                skipSingleLineComment()
                                continue
                            }
                            // '*' -> {
                            //     skipMultiLineComment()
                            //     continue
                            // }
                            '=' -> build(ASSIGN_DIVIDE).alsoAdvance()
                            else -> build()
                        }
                    }
                    BIT_AND -> {
                        // & &= && &&=
                        advance()
                        return when (curr) {
                            '&' -> select('=', ASSIGN_AND, AND)
                            '=' -> build(ASSIGN_BIT_AND).alsoAdvance()
                            else -> build()
                        }
                    }
                    BIT_OR -> {
                        // | |= || ||=
                        advance()
                        return when (curr) {
                            '|' -> select('=', ASSIGN_OR, OR)
                            '=' -> build(ASSIGN_BIT_OR).alsoAdvance()
                            else -> build()
                        }
                    }
                    // ^ ^=
                    BIT_XOR -> return selectIf('=', ASSIGN_BIT_XOR)
                    PERIOD -> {
                        // . ...
                        advance()
                        return if (curr == '.' && peek() == '.') {
                            advance()
                            advance()
                            build(ELLIPSIS)
                        } else build(PERIOD)
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
                    WHITE_SPACE -> {
                        skipWhiteSpaceOrLineTerminator()
                        continue
                    }
                    else -> if (curr.isEndOfInput) build(if (hasError) ILLEGAL else EOS)
                }
            } while (builder.type == UNINITIALIZED)
        }
        return builder.build()
    }
}

private const val unicodeEscapeSequencePrefix = "\\u"
private const val hexEscapeSequencePrefix = "\\x"

private val Char.isEndOfInput get() =
    this == Source.endOfInput
