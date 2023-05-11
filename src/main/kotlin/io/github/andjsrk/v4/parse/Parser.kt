package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.BinaryOperationType as BinaryOp
import io.github.andjsrk.v4.WasSuccessful
import io.github.andjsrk.v4.error.Error
import io.github.andjsrk.v4.error.SyntaxError
import io.github.andjsrk.v4.parse.ReservedWord.*
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.node.BinaryExpressionNode
import io.github.andjsrk.v4.parse.node.ExpressionNode
import io.github.andjsrk.v4.parse.node.IdentifierNode
import io.github.andjsrk.v4.parse.node.ThisNode
import io.github.andjsrk.v4.parse.node.ArrayBindingPatternNode
import io.github.andjsrk.v4.parse.node.MaybeSpreadNode
import io.github.andjsrk.v4.parse.node.NonSpreadNode
import io.github.andjsrk.v4.parse.node.ArrayLiteralNode
import io.github.andjsrk.v4.thenAlso
import io.github.andjsrk.v4.tokenize.Token
import io.github.andjsrk.v4.tokenize.TokenType
import io.github.andjsrk.v4.tokenize.TokenType.*
import io.github.andjsrk.v4.tokenize.Tokenizer
import io.github.andjsrk.v4.util.isOneOf

class Parser(private val tokenizer: Tokenizer) {
    private var currToken = tokenizer.getNextToken()
    var error: ErrorWithRange? = null
        private set
    lateinit var errorArgs: Array<String>
    val hasError get() =
        error != null
    private var isLastStatementTerminated = true
    private fun advance(): Token {
        val prev = currToken
        currToken = tokenizer.getNextToken()
        return prev
    }
    private fun <T> T.alsoAdvance() =
        also { advance() }
    private fun skip(tokenType: TokenType): WasSuccessful =
        (currToken.type == tokenType).thenAlso {
            advance()
        }
    private fun takeIfMatches(tokenType: TokenType) =
        if (currToken.type == tokenType) advance()
        else null
    private fun takeIfMatchesKeyword(keyword: ReservedWord) =
        if (currToken.isKeyword(keyword)) advance()
        else null
    private inline fun expect(tokenType: TokenType, check: (Token) -> Boolean = { true }) =
        if (currToken.type == tokenType && check(currToken)) advance()
        else reportUnexpectedToken()
    private fun expectKeyword(keyword: ReservedWord) =
        expect(IDENTIFIER) { it.rawContent == keyword.value }
    private fun reportErrorMessage(kind: Error, range: Range = currToken.range, vararg args: String): Nothing? {
        error = ErrorWithRange(kind, range)
        errorArgs = arrayOf(*args)
        return null
    }
    private fun reportUnexpectedToken(token: Token = currToken): Nothing? {
        val kind = when (token.type) {
            EOS -> SyntaxError.UNEXPECTED_EOS
            NUMBER, BIGINT -> SyntaxError.UNEXPECTED_TOKEN_NUMBER
            STRING -> SyntaxError.UNEXPECTED_TOKEN_STRING
            IDENTIFIER -> SyntaxError.UNEXPECTED_TOKEN_IDENTIFIER
            TEMPLATE_HEAD, TEMPLATE_MIDDLE, TEMPLATE_TAIL, TEMPLATE_FULL -> SyntaxError.UNEXPECTED_TEMPLATE_STRING
            ILLEGAL -> SyntaxError.INVALID_OR_UNEXPECTED_TOKEN
            else -> return reportErrorMessage(
                SyntaxError.UNEXPECTED_TOKEN,
                token.range,
                token.type.staticContent ?: token.rawContent.ifEmpty { token.type.name },
            )
        }
        return reportErrorMessage(kind, token.range)
    }
    private fun <R> ifHasNoError(lazyValue: Lazy<R>): R? =
        if (hasError) null
        else lazyValue.value
    private inline fun ifHasNoError(crossinline block: () -> Unit) =
        ifHasNoError(lazy {
            block()
            null
        })
    private fun checkNoLineTerminatorBeforeCurrent() =
        currToken.not { isPrevLineTerminator }
    // <editor-fold desc="expressions">
    // <editor-fold desc="primary expressions">
    private fun Token.toIdentifier() =
        IdentifierNode(rawContent, range).also {
            assert(type == IDENTIFIER)
        }
    /**
     * Parses [Identifier](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-Identifier).
     */
    @Careful
    private fun parseIdentifier(): IdentifierNode? {
        if (currToken.type != IDENTIFIER) return null
        if (ReservedWord.values().any { it.not { isContextual } && currToken.isKeyword(it, true) }) return null

        return currToken.toIdentifier().alsoAdvance()
    }
    /**
     * Parses [IdentifierName](https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#prod-IdentifierName).
     */
    @Careful
    private fun parseIdentifierName() =
        takeIfMatches(IDENTIFIER)?.toIdentifier()
    @Careful
    private fun parseNumberLiteral() =
        takeIfMatches(NUMBER)?.let(::NumberLiteralNode)
    @Careful
    private fun parseBigintLiteral() =
        takeIfMatches(BIGINT)?.let(::BigintLiteralNode)
    @Careful
    private fun parseStringLiteral() =
        takeIfMatches(STRING)?.let(::StringLiteralNode)
    @Careful
    private fun parsePrimitiveLiteral(): LiteralNode? =
        when (currToken.type) {
            STRING -> parseStringLiteral()
            NUMBER -> parseNumberLiteral()
            BIGINT -> parseBigintLiteral()
            IDENTIFIER -> when {
                currToken.isKeyword(NULL, true) -> NullLiteralNode(currToken)
                currToken.isKeyword(TRUE, true) || currToken.isKeyword(FALSE, true) -> BooleanLiteralNode(currToken)
                else -> null
            }
                ?.alsoAdvance()
            else -> null
        }
    @ReportsErrorDirectly
    private fun parseArrayElement(): MaybeSpreadNode? {
        val ellipsisToken = takeIfMatches(ELLIPSIS)
        val expr = parseAssignment() ?: return null

        return (
            if (ellipsisToken != null) SpreadNode(expr, ellipsisToken.range..expr.range)
            else NonSpreadNode(expr)
        )
    }
    /**
     * Parses [ArrayLiteral](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ArrayLiteral).
     */
    @ReportsErrorDirectly
    private fun parseArrayLiteral(): ArrayLiteralNode? {
        val elements = mutableListOf<MaybeSpreadNode>()

        val leftBracketTokenRange = takeIfMatches(LEFT_BRACKET)?.range ?: return null
        // allow trailing comma, but disallow sparse array
        var skippedComma = true
        while (currToken.type != RIGHT_BRACKET) {
            if (!skippedComma) return reportUnexpectedToken()
            elements += parseArrayElement() ?: return null
            skippedComma = skip(COMMA)
        }
        val rightBracketTokenRange = expect(RIGHT_BRACKET)?.range ?: return null

        return ArrayLiteralNode(elements.toList(), leftBracketTokenRange..rightBracketTokenRange)
    }
    /**
     * Parses [ComputedPropertyName](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ComputedPropertyName).
     */
    @ReportsErrorDirectly
    private fun parseComputedPropertyName(): ComputedPropertyKeyNode? {
        val startRange = takeIfMatches(LEFT_BRACKET)?.range ?: return null
        val expression = parseExpression() ?: return null
        val endRange = expect(RIGHT_BRACKET)?.range ?: return null

        return ComputedPropertyKeyNode(expression, startRange..endRange)
    }
    /**
     * Parses [LiteralPropertyName](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-LiteralPropertyName).
     */
    @Careful
    private fun parseLiteralPropertyName() =
        parseIdentifierName() ?: parseStringLiteral() ?: parseNumberLiteral() // no bigint literal
    /**
     * Parses [PropertyName](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-PropertyName).
     */
    @Careful(false)
    private fun parsePropertyName() =
        parseLiteralPropertyName() ?: parseComputedPropertyName()
    /**
     * Parses [PropertyDefinition](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-PropertyDefinition).
     */
    @ReportsErrorDirectly
    private fun parsePropertyDefinition(allowCoverInitializedName: Boolean = false): ObjectElementNode? {
        return when (currToken.type) {
            ELLIPSIS -> {
                val spreadToken = advance()
                val expression = parseAssignment() ?: return null
                SpreadNode(expression, spreadToken.range..expression.range)
            }
            else -> {
                val identifier = parseIdentifier()
                val propertyName = identifier ?: parsePropertyName() ?: return reportUnexpectedToken()
                when (currToken.type) {
                    COLON -> { // { a: b }
                        advance()
                        val value = parseAssignment() ?: return null
                        PropertyNode(propertyName, value)
                    }
                    ASSIGN -> { // CoverInitializedName
                        if (identifier == null) return reportUnexpectedToken()
                        advance()
                        val default = parseAssignment() ?: return null
                        if (!allowCoverInitializedName) return reportErrorMessage(
                            SyntaxError.INVALID_COVER_INITIALIZED_NAME,
                            identifier.range..default.range,
                        )
                        CoverInitializedNameNode(identifier, default)
                    }
                    else ->
                        parseMethodLikeObjectElement(propertyName)
                            ?: ifHasNoError(lazy { PropertyShorthandNode(propertyName) })
                }
            }
        }
    }
    /**
     * Parses [ObjectLiteral](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ObjectLiteral).
     */
    @ReportsErrorDirectly
    private fun parseObjectLiteral(allowDestructuring: Boolean = false): ObjectLiteralNode? {
        val elements = mutableListOf<ObjectElementNode>()

        val leftBraceTokenRange = takeIfMatches(LEFT_BRACE)?.range ?: return null
        var skippedComma = true
        while (currToken.type != RIGHT_BRACE) {
            if (!skippedComma) return reportUnexpectedToken()
            val element = parsePropertyDefinition(allowDestructuring) ?: return null
            elements += element
            skippedComma = skip(COMMA)
        }
        val rightBraceTokenRange = expect(RIGHT_BRACE)?.range ?: return null

        return ObjectLiteralNode(elements.toList(), leftBraceTokenRange..rightBraceTokenRange)
    }
    @Careful
    private fun parseThis(): ThisNode? =
        takeIfMatchesKeyword(THIS)?.let {
            ThisNode(it.range)
        }
    @Careful
    private fun parseBindingIdentifier() =
        parseIdentifier()
    /**
     * Parses [BindingRestElement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-BindingRestElement) if `allowBindingPattern` is `true`,
     * [BindingRestProperty](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-BindingRestProperty) otherwise.
     */
    @ReportsErrorDirectly
    private fun parseBindingRestElement(allowBindingPattern: Boolean = true): RestNode? {
        val restTokenRange = expect(ELLIPSIS)?.range ?: return null
        val binding = parseBindingIdentifier()
            ?: lazy { parseBindingPattern() }.takeIf { allowBindingPattern }?.value
            ?: return reportUnexpectedToken()
        when (currToken.type) {
            COMMA -> return reportErrorMessage(SyntaxError.ELEMENT_AFTER_REST)
            ASSIGN -> return reportErrorMessage(SyntaxError.REST_DEFAULT_INITIALIZER)
            else -> {}
        }
        return RestNode(binding, restTokenRange..binding.range)
    }
    /**
     * Parses [BindingElement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-BindingElement).
     */
    @ReportsErrorDirectly
    private fun parseBindingElement(): NonRestNode? {
        val binding = parseBindingIdentifier() ?: parseBindingPattern() ?: return reportErrorMessage(SyntaxError.INVALID_DESTRUCTURING_TARGET)
        takeIfMatches(ASSIGN) ?: return NonRestNode(binding, null)
        val default = parseAssignment() ?: return null
        return NonRestNode(binding, default)
    }
    @ReportsErrorDirectly
    private fun parseArrayBindingPattern(): ArrayBindingPatternNode? {
        val startRange = takeIfMatches(LEFT_BRACKET)?.range ?: return null
        val items = mutableListOf<MaybeRestNode>()

        var skippedComma = true
        while (currToken.type != RIGHT_BRACKET) {
            if (!skippedComma) return reportUnexpectedToken()
            if (currToken.type == ELLIPSIS) {
                items += parseBindingRestElement() ?: return null
                break
            } else {
                val bindingElement = parseBindingElement() ?: return null
                skippedComma = skip(COMMA)
                items += bindingElement
            }
        }
        val endRange = expect(RIGHT_BRACKET)?.range ?: return null

        return ArrayBindingPatternNode(items.toList(), startRange..endRange)
    }
    /**
     * Parses [BindingProperty](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-BindingProperty).
     */
    @Careful(false)
    private fun parseBindingProperty(): NonRestObjectPropertyNode? {
        // PropertyName contains BindingIdentifier, so it is not need to parse BindingIdentifier separately
        val left = parsePropertyName() ?: return null
        return when (currToken.type) {
            COLON -> {
                advance()
                val bindingElement = parseBindingElement() ?: return null
                return NonRestObjectPropertyNode(left, bindingElement.binding, bindingElement.default)
            }
            ASSIGN -> {
                advance()
                val default = parseAssignment() ?: return null
                NonRestObjectPropertyNode(left, left, default)
            }
            else -> NonRestObjectPropertyNode(left, left, null)
        }
    }
    @ReportsErrorDirectly
    private fun parseObjectBindingPattern(): ObjectBindingPatternNode? {
        val startRange = takeIfMatches(LEFT_BRACE)?.range ?: return null
        val items = mutableListOf<MaybeRestNode>()

        var skippedComma = true
        while (currToken.type != RIGHT_BRACE) {
            if (!skippedComma) return reportUnexpectedToken()
            if (currToken.type == ELLIPSIS) {
                items += parseBindingRestElement(false) ?: return null
                break
            } else {
                val bindingProperty = parseBindingProperty() ?: return null
                skippedComma = skip(COMMA)
                items += bindingProperty
            }
        }
        val endRange = expect(RIGHT_BRACE)?.range ?: return null

        return ObjectBindingPatternNode(items.toList(), startRange..endRange)
    }
    @Careful(false)
    private fun parseBindingPattern() =
        when (currToken.type) {
            LEFT_BRACE -> parseObjectBindingPattern()
            LEFT_BRACKET -> parseArrayBindingPattern()
            else -> null
        }
    @ReportsErrorDirectly
    private fun parseCoverParenthesizedExpressionAndArrowParameterList(): CoverParenthesizedExpressionAndArrowParameterListNode? {
        val leftParenRange = takeIfMatches(LEFT_PARENTHESIS)?.range ?: return null

        // should not use parseExpression because it will ignore rest parameter syntax and report it as unexpected token

        val items = mutableListOf<ExpressionOrBindingPatternNode>()
        var skippedComma = true
        while (currToken.type != RIGHT_PARENTHESIS) {
            if (!skippedComma) return reportUnexpectedToken()
            if (currToken.type == ELLIPSIS) {
                items += parseBindingRestElement() ?: return null
                break
            } else {
                items += when (currToken.type) {
                    LEFT_BRACE -> parseObjectLiteral(true)
                    else -> parseAssignment()
                } ?: return null
            }
            skippedComma = skip(COMMA)
        }
        val rightParenRange = expect(RIGHT_PARENTHESIS)?.range ?: return null

        return CoverParenthesizedExpressionAndArrowParameterListNode(items.toList(), leftParenRange..rightParenRange)
    }
    /**
     * Parses [PrimaryExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-PrimaryExpression).
     */
    @Careful(false)
    private fun parsePrimaryExpression(): ExpressionNode? =
        parsePrimitiveLiteral() ?: parseIdentifier() ?: when (currToken.type) {
            IDENTIFIER -> // now there are only keywords except primitive literals
                parseThis() // temp
            LEFT_PARENTHESIS -> parseCoverParenthesizedExpressionAndArrowParameterList()
            LEFT_BRACE -> parseObjectLiteral()
            LEFT_BRACKET -> parseArrayLiteral()
            else -> null
        }
    // </editor-fold>
    @Careful(false)
    private fun parseArgument(): MaybeSpreadNode? {
        if (currToken.type == ELLIPSIS) {
            val spreadTokenRange = advance().range
            val expr = parseAssignment() ?: return null
            return SpreadNode(expr, spreadTokenRange..expr.range)
        }
        val expr = parseAssignment() ?: return null
        return NonSpreadNode(expr)
    }
    /**
     * Parses [Arguments](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-Arguments).
     */
    @ReportsErrorDirectly
    private fun parseArguments(): ArgumentsNode? {
        val elements = mutableListOf<MaybeSpreadNode>()

        val startRange = expect(LEFT_PARENTHESIS)?.range ?: return null
        var skippedComma = true
        while (currToken.type != RIGHT_PARENTHESIS) {
            if (!skippedComma) return reportUnexpectedToken()
            elements += parseArgument() ?: return null
            skippedComma = skip(COMMA)
        }
        val endRange = expect(RIGHT_PARENTHESIS)?.range ?: return null

        return ArgumentsNode(elements.toList(), startRange..endRange)
    }
    /**
     * Parses [MemberExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-MemberExpression).
     * Returns [NewExpressionNode] or [NormalCallNode] if possible.
     */
    @ReportsErrorDirectly
    private tailrec fun parseMemberExpression(`object`: ExpressionNode?, parsingNew: Boolean = false): ExpressionNode? {
        if (`object` == null) { // base case
            val primary = parsePrimaryExpression() ?: return null
            return parseMemberExpression(primary, parsingNew)
        }

        val member = MemberExpressionNode.Unsealed()
        member.`object` = `object`
        val questionDotToken = takeIfMatches(QUESTION_DOT)
        member.isOptionalChain = questionDotToken != null

        if (parsingNew && member.isOptionalChain) return reportErrorMessage(SyntaxError.NEW_OPTIONAL_CHAIN)

        when (currToken.type) {
            LEFT_BRACKET -> {
                advance()
                member.property = parseExpression() ?: return reportUnexpectedToken()
                member.endRange = expect(RIGHT_BRACKET)?.range ?: return null
                member.isComputed = true
            }
            DOT -> {
                if (member.isOptionalChain) return reportUnexpectedToken()
                advance()
                member.property = parseIdentifierName() ?: return reportUnexpectedToken()
                member.endRange = member.property.range
            }
            IDENTIFIER -> {
                if (member.not { isOptionalChain }) return `object`
                member.property = parseIdentifierName()!!
                member.endRange = member.property.range
            }
            else -> {
                if (currToken.type == LEFT_PARENTHESIS && !parsingNew) return parseCall(`object`, member.isOptionalChain)

                // if it is neither member access nor call, it is an unexpected token
                if (member.isOptionalChain) return reportUnexpectedToken()

                return `object`
            }
        }
        return parseMemberExpression(member.toSealed(), parsingNew)
    }
    /**
     * Parses [SuperCall](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-SuperCall).
     */
    @ReportsErrorDirectly
    private fun parseSuperCall(): SuperCallNode? {
        val superNode = takeIfMatchesKeyword(SUPER)
            ?.let { SuperNode(it.range) }
            ?: return null
        if (currToken.type != LEFT_PARENTHESIS) return reportErrorMessage(SyntaxError.UNEXPECTED_SUPER, superNode.range)
        val args = parseArguments() ?: return null
        expect(RIGHT_PARENTHESIS) ?: return null

        return SuperCallNode(superNode, args)
    }
    /**
     * Parses [ImportCall](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ImportCall).
     */
    @ReportsErrorDirectly
    private fun parseImportCall(): ImportCallNode? {
        val importNode = takeIfMatchesKeyword(IMPORT)
            ?.let { ImportNode(it.range) }
            ?: return null
        takeIfMatches(LEFT_PARENTHESIS) ?: return null
        val pathSpecifier = parseExpression() ?: return null
        val endRange = expect(RIGHT_PARENTHESIS)?.range ?: return null

        return ImportCallNode(importNode, pathSpecifier, endRange)
    }
    /**
     * Parses [CallExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-CallExpression).
     */
    @Careful(false)
    private fun parseCall(callee: ExpressionNode?, isOptionalChain: Boolean = false): ExpressionNode? {
        return parseMemberExpression(
            if (callee == null) when {
                currToken.isKeyword(SUPER) -> parseSuperCall()
                currToken.isKeyword(IMPORT) -> parseImportCall()
                else -> null
            }
            else {
                if (currToken.type != LEFT_PARENTHESIS) return callee.takeUnless { isOptionalChain }
                val args = parseArguments() ?: return null
                NormalCallNode(callee, args, isOptionalChain)
            }
        )
    }
    /**
     * Parses [`new` expression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-MemberExpression).
     */
    @Careful(false)
    private fun parseNewExpression(): ExpressionNode? {
        val newToken = takeIfMatchesKeyword(NEW)
        val isNewExpr = newToken != null
        val memberExpr = parseMemberExpression(null, isNewExpr) ?: return null

        if (!isNewExpr) return memberExpr
        requireNotNull(newToken)

        val args = parseArguments() ?: return null
        return NewExpressionNode(memberExpr, args, newToken.range)
    }
    private fun <T> Iterable<Lazy<T>>.foldElvisIfHasNoError() =
        foldElvis { ifHasNoError(it) }
    /**
     * Parses [LeftHandSideExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-LeftHandSideExpression).
     */
    @Careful(false)
    private fun parseLeftHandSideExpression() =
        listOf(
            lazy { parseNewExpression() },
            lazy { parseCall(null) },
        )
            .foldElvisIfHasNoError()
    /**
     * Parses [UpdateExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-UpdateExpression).
     */
    @Careful(false)
    private fun parseUpdate(): ExpressionNode? {
        return when (currToken.type) {
            INCREMENT, DECREMENT -> {
                val token = advance()
                val leftHandSideExpr = parseLeftHandSideExpression() ?: return null
                UpdateNode(
                    leftHandSideExpr,
                    UnaryOperationType.valueOf(token.type.name),
                    token.range,
                    true,
                )
            }
            else -> {
                val leftHandSideExpr = parseLeftHandSideExpression() ?: return null
                if (currToken.not { isPrevLineTerminator } && currToken.type.isOneOf(INCREMENT, DECREMENT)) {
                    val token = advance()
                    UpdateNode(
                        leftHandSideExpr,
                        UnaryOperationType.valueOf(token.type.name),
                        token.range,
                        false,
                    )
                } else leftHandSideExpr
            }
        }
    }
    /**
     * Parses [UnaryExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-UnaryExpression).
     */
    @Careful(false)
    private fun parseUnaryExpression(): ExpressionNode? {
        val operation = when {
            currToken.isKeyword(AWAIT) -> UnaryOperationType.AWAIT
            currToken.isKeyword(VOID) -> UnaryOperationType.VOID
            currToken.isKeyword(TYPEOF) -> UnaryOperationType.TYPEOF
            else -> when (currToken.type) {
                MINUS -> UnaryOperationType.MINUS
                NOT -> UnaryOperationType.NOT
                BITWISE_NOT -> UnaryOperationType.BITWISE_NOT
                else -> return parseUpdate()
            }
        }
        val operationToken = advance()
        val operand = parseUnaryExpression() ?: return null

        return UnaryExpressionNode(operand, operation, operationToken.range)
    }
    /**
     * Parses [ExponentiationExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ExponentiationExpression).
     */
    @ReportsErrorDirectly
    private fun parseExponentiation(): ExpressionNode? {
        val expr = parseUnaryExpression() ?: return null
        if (currToken.type != EXPONENTIAL) return expr
        val exponentiationToken = advance()
        if (expr is UnaryExpressionNode && expr !is UpdateNode) return reportErrorMessage(
            SyntaxError.UNEXPECTED_TOKEN_UNARY_EXPONENTIATION,
            expr.range..exponentiationToken.range,
        )
        val exponentiationExpr = parseExponentiation() ?: return null
        return BinaryExpressionNode(expr, exponentiationExpr, BinaryOp.EXPONENTIAL)
    }
    /**
     * Parses [MultiplicativeExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-MultiplicativeExpression).
     */
    @Careful(false)
    private tailrec fun parseMultiplication(left: BinaryExpressionNode? = null): ExpressionNode? {
        val exponentiationExpr = left ?: parseExponentiation() ?: return null
        val operation = when (currToken.type) {
            MULTIPLY -> BinaryOp.MULTIPLY
            DIVIDE -> BinaryOp.DIVIDE
            MOD -> BinaryOp.MOD
            else -> return exponentiationExpr
        }
        advance()
        val right = parseExponentiation() ?: return null
        return parseMultiplication(BinaryExpressionNode(exponentiationExpr, right, operation))
    }
    /**
     * Parses [AdditiveExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-AdditiveExpression).
     */
    @Careful(false)
    private tailrec fun parseAddition(left: BinaryExpressionNode? = null): ExpressionNode? {
        val multiplicativeExpr = left ?: parseMultiplication() ?: return null
        val operation = when (currToken.type) {
            PLUS -> BinaryOp.PLUS
            MINUS -> BinaryOp.MINUS
            else -> return multiplicativeExpr
        }
        advance()
        val right = parseMultiplication() ?: return null
        return parseAddition(BinaryExpressionNode(multiplicativeExpr, right, operation))
    }
    /**
     * Parses [ShiftExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ShiftExpression).
     */
    @Careful(false)
    private tailrec fun parseShift(left: BinaryExpressionNode? = null): ExpressionNode? {
        val additiveExpr = left ?: parseAddition() ?: return null
        val operation = when (currToken.type) {
            SHL -> BinaryOp.SHL
            SAR -> BinaryOp.SAR
            SHR -> BinaryOp.SHR
            else -> return additiveExpr
        }
        advance()
        val right = parseAddition() ?: return null
        return parseShift(BinaryExpressionNode(additiveExpr, right, operation))
    }
    /**
     * Parses [RelationalExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-RelationalExpression).
     */
    @Careful(false)
    private tailrec fun parseRelation(left: BinaryExpressionNode? = null): ExpressionNode? {
        val shiftExpr = left ?: parseShift() ?: return null
        val operation = when (currToken.type) {
            LT -> BinaryOp.LT
            GT -> BinaryOp.GT
            LT_EQ -> BinaryOp.LT_EQ
            GT_EQ -> BinaryOp.GT_EQ
            else -> when {
                currToken.isKeyword(INSTANCEOF) -> BinaryOp.INSTANCEOF
                currToken.isKeyword(IN) -> BinaryOp.IN
                else -> return shiftExpr
            }
        }
        advance()
        val right = parseShift() ?: return null
        return parseRelation(BinaryExpressionNode(shiftExpr, right, operation))
    }
    /**
     * Parses [EqualityExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-EqualityExpression).
     */
    @Careful(false)
    private tailrec fun parseEquality(left: BinaryExpressionNode? = null): ExpressionNode? {
        val relationalExpr = left ?: parseRelation() ?: return null
        if (currToken.type !in EQ..NOT_EQ_STRICT) return relationalExpr
        val operation = BinaryOp.fromTokenType(currToken.type)
        advance()
        val right = parseRelation() ?: return null
        return parseEquality(BinaryExpressionNode(relationalExpr, right, operation))
    }
    private fun parseGeneralBinaryExpression(opTokenType: TokenType, parseInner: () -> ExpressionNode?): ExpressionNode? {
        tailrec fun parse(left: BinaryExpressionNode?): ExpressionNode? {
            val expr = left ?: parseInner() ?: return null
            takeIfMatches(opTokenType) ?: return expr
            val right = parseInner() ?: return null
            return parse(BinaryExpressionNode(expr, right, BinaryOp.fromTokenType(opTokenType)))
        }
        return parse(null)
    }
    /**
     * Parses [BitwiseANDExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-BitwiseANDExpression).
     */
    @Careful(false)
    private fun parseBitwiseAnd() =
        parseGeneralBinaryExpression(BITWISE_AND, ::parseEquality)
    /**
     * Parses [BitwiseXORExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-BitwiseXORExpression).
     */
    @Careful(false)
    private fun parseBitwiseXor() =
        parseGeneralBinaryExpression(BITWISE_XOR, ::parseBitwiseAnd)
    /**
     * Parses [BitwiseORExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-BitwiseORExpression).
     */
    @Careful(false)
    private fun parseBitwiseOrExpression() =
        parseGeneralBinaryExpression(BITWISE_OR, ::parseBitwiseXor)
    /**
     * Parses [LogicalANDExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-LogicalANDExpression).
     */
    @Careful(false)
    private fun parseLogicalAnd() =
        parseGeneralBinaryExpression(AND, ::parseBitwiseOrExpression)
    /**
     * Parses [LogicalORExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-LogicalORExpression).
     */
    @Careful(false)
    private fun parseLogicalOr() =
        parseGeneralBinaryExpression(OR, ::parseLogicalAnd)
    /**
     * Parses [CoalesceExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-CoalesceExpression).
     * Note that the method takes non-null parse result from [parseLogicalOr].
     */
    @ReportsErrorDirectly
    private tailrec fun parseCoalesce(left: ExpressionNode): ExpressionNode? {
        val coalesceToken = takeIfMatches(COALESCE) ?: return left
        if (left is BinaryExpressionNode && left.operation.isOneOf(BinaryOp.OR, BinaryOp.AND)) return reportUnexpectedToken(coalesceToken)
        val right = parseBitwiseOrExpression() ?: return null
        return parseCoalesce(BinaryExpressionNode(left, right, BinaryOp.COALESCE))
    }
    /**
     * Parses [ShortCircuitExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ShortCircuitExpression).
     */
    @Careful(false)
    private fun parseShortCircuit() =
        parseLogicalOr()
            ?.let(::parseCoalesce)
    /**
     * Parses [ConditionalExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ConditionalExpression).
     */
    @ReportsErrorDirectly
    private fun parseConditionalExpression(): ExpressionNode? {
        val shortCircuitExpr = parseShortCircuit() ?: return null
        takeIfMatches(CONDITIONAL) ?: return shortCircuitExpr
        val consequent = parseAssignment() ?: return null
        expect(COLON) ?: return null
        val alternative = parseAssignment() ?: return null
        return ConditionalExpressionNode(shortCircuitExpr, consequent, alternative)
    }
    /**
     * Parses [YieldExpression](https://tc39.es/ecma262/multipage/ecmascript-language-functions-and-classes.html#prod-YieldExpression).
     */
    @Careful(false)
    private fun parseYield(): YieldNode? {
        val yieldTokenRange = takeIfMatchesKeyword(YIELD)?.range ?: return null
        if (currToken.isPrevLineTerminator) return YieldNode(null, yieldTokenRange)
        val isDelegate = takeIfMatches(MULTIPLY) != null
        val expr = parseAssignment() ?: return (
            if (isDelegate) null
            else YieldNode(null, yieldTokenRange)
        )
        val range = yieldTokenRange..expr.range
        return (
            if (isDelegate) DelegatedYieldNode(expr, range)
            else YieldNode(expr, range)
        )
    }
    // <editor-fold desc="arrow function">
    /**
     * Parses [ConciseBody](https://tc39.es/ecma262/multipage/ecmascript-language-functions-and-classes.html#prod-ConciseBody).
     */
    @Careful(false)
    private fun parseConciseBody() =
        if (currToken.type == LEFT_BRACE) parseBlockStatement()
        else parseAssignment()
    @ReportsErrorDirectly
    private fun parseArrowFunctionWithoutParenthesis(parameter: IdentifierNode): ArrowFunctionNode? {
        // current token is =>

        if (!checkNoLineTerminatorBeforeCurrent()) return reportUnexpectedToken()
        advance()

        val body = parseConciseBody() ?: return null

        return ArrowFunctionNode(
            FormalParametersNode(listOf(parameter.toNonRest() ?: neverHappens()), parameter.range),
            body,
            false,
            false,
            parameter.range..body.range,
        )
    }
    @ReportsErrorDirectly
    private fun Node.toNonRestNodeRight(carefully: Boolean = false): IdentifierOrBindingPatternNode? =
        when (this) {
            is IdentifierNode -> this
            is CollectionLiteralNode<*> -> this.toBindingPattern()
            else -> {
                if (!carefully) reportErrorMessage(
                    SyntaxError.INVALID_DESTRUCTURING_TARGET,
                    range,
                )
                null
            }
        }
    @Careful(false)
    private fun Node.toNonRest(carefully: Boolean = false) =
        this.toNonRestNodeRight(carefully)?.wrapNonRest()
    /**
     * https://tc39.es/ecma262/multipage/ecmascript-language-functions-and-classes.html#sec-arrow-function-definitions-static-semantics-early-errors
     * Note that this will be applied to methods as well because they are not contextually allowed anymore.
     */
    private fun <N: NonRestNode> N.withEarlyErrorChecks() =
        when {
            default is YieldNode ->
                reportErrorMessage(SyntaxError.YIELD_IN_PARAMETER, range)
            default is UnaryExpressionNode && default.operation == UnaryOperationType.AWAIT ->
                reportErrorMessage(SyntaxError.AWAIT_EXPRESSION_FORMAL_PARAMETER, range)
            else -> this
        }
    @Careful(false)
    private fun MaybeSpreadNode.toMaybeRest(): MaybeRestNode? {
        return when (this) {
            is NonSpreadNode -> when (val expr = expression) {
                is IdentifierNode, is CollectionLiteralNode<*> -> expr.toNonRest(true)
                is BinaryExpressionNode -> {
                    val left = expr.left.toNonRestNodeRight(true) ?: return null
                    NonRestNode(left, expr.right)
                        .withEarlyErrorChecks()
                }
                else -> null
            }
            is SpreadNode -> RestNode(expression, range)
        }
    }
    @ReportsErrorDirectly
    private fun ArrayLiteralNode.toArrayBindingPattern(): ArrayBindingPatternNode? {
        return ArrayBindingPatternNode(
            elements.map {
                it.toMaybeRest() ?: return reportErrorMessage(SyntaxError.INVALID_DESTRUCTURING_TARGET)
            },
            range,
        )
    }
    @ReportsErrorDirectly
    private fun ObjectLiteralNode.toObjectBindingPattern(): ObjectBindingPatternNode? {
        return ObjectBindingPatternNode(
            elements.map {
                when (it) {
                    is SpreadNode -> {
                        if (it.expression !is IdentifierNode) return reportErrorMessage(SyntaxError.INVALID_REST_BINDING_PATTERN)
                        RestNode(it.expression, it.range)
                    }
                    is CoverInitializedNameNode ->
                        NonRestObjectPropertyNode(it.key, it.key, it.default)
                            .withEarlyErrorChecks()
                            ?: return null
                    is PropertyShorthandNode -> {
                        if (it.name !is IdentifierNode) return reportErrorMessage(SyntaxError.INVALID_REST_BINDING_PATTERN)
                        NonRestObjectPropertyNode(it.name, it.name, null)
                    }
                    is PropertyNode -> when (val value = it.value) {
                        is BinaryExpressionNode ->
                            NonRestObjectPropertyNode(it.key, value.left, value.right)
                                .withEarlyErrorChecks()
                                ?: return null
                        else -> NonRestObjectPropertyNode(
                            it.key,
                            value.toNonRestNodeRight() ?: return null,
                            null,
                        )
                    }
                    else -> neverHappens()
                }
            },
            range,
        )
    }
    @Careful(false)
    private fun CollectionLiteralNode<*>.toBindingPattern() =
        when (this) {
            is ArrayLiteralNode -> this.toArrayBindingPattern()
            is ObjectLiteralNode -> this.toObjectBindingPattern()
        }
    private val Node.invalidDestructuringRange: Range? get() =
        when (this) {
            is IdentifierNode,
            is RestNode,
                -> null
            is CollectionLiteralNode<*> ->
                elements.foldElvis {
                    if (it is MaybeSpreadNode) it.invalidDestructuringRange
                    else it.invalidDestructuringRange
                }
            is BinaryExpressionNode ->
                if (operation == BinaryOp.ASSIGN) left.invalidDestructuringRange
                else range
            is PropertyNode -> value.invalidDestructuringRange
            is PropertyShorthandNode -> name.invalidDestructuringRange
            is CoverInitializedNameNode -> null
            else -> range
        }
    private val MaybeSpreadNode.invalidDestructuringRange get() =
        when (this) {
            is NonSpreadNode -> expression.invalidDestructuringRange
            is SpreadNode ->
                if (expression is IdentifierNode) null
                else range.also { reportErrorMessage(SyntaxError.INVALID_REST_BINDING_PATTERN, range) }
        }
    private fun CoverParenthesizedExpressionAndArrowParameterListNode.findInvalidDestructuringRange() =
        elements.foldElvis { it.invalidDestructuringRange }
    @ReportsErrorDirectly
    private fun CoverParenthesizedExpressionAndArrowParameterListNode.toFormalParameters(): FormalParametersNode? {
        val invalidRange = findInvalidDestructuringRange()
        if (invalidRange != null) return ifHasNoError {
            reportErrorMessage(SyntaxError.INVALID_DESTRUCTURING_TARGET, invalidRange)
        }

        return FormalParametersNode(
            elements.map {
                when (it) {
                    is IdentifierNode -> it.wrapNonRest()
                    is BinaryExpressionNode ->
                        NonRestNode(it.left, it.right)
                            .withEarlyErrorChecks()
                            ?: return null
                    is CollectionLiteralNode<*> ->
                        it.toBindingPattern()
                            ?.wrapNonRest()
                            ?: return null
                    is RestNode -> it
                    else -> return reportErrorMessage(SyntaxError.INVALID_DESTRUCTURING_TARGET, it.range)
                }
            },
            range,
        )
    }
    /**
     * Parses [ArrowFormalParameters](https://tc39.es/ecma262/multipage/ecmascript-language-functions-and-classes.html#prod-ArrowFormalParameters).
     */
    @ReportsErrorDirectly
    private fun parseFormalParameters(): FormalParametersNode? {
        val startRange = takeIfMatches(LEFT_PARENTHESIS)?.range ?: return null
        val elements = mutableListOf<MaybeRestNode>()
        var skippedComma = true
        while (currToken.type != RIGHT_PARENTHESIS) {
            if (!skippedComma) return reportUnexpectedToken()
            if (currToken.type == ELLIPSIS) {
                elements += parseBindingRestElement() ?: return null
                break
            }
            elements += parseBindingElement() ?: return null
            skippedComma = skip(COMMA)
        }
        val endRange = expect(RIGHT_PARENTHESIS)?.range ?: return null

        return FormalParametersNode(elements, startRange..endRange)
    }
    @ReportsErrorDirectly
    private fun parseArrowFunctionByCover(cover: CoverParenthesizedExpressionAndArrowParameterListNode): ArrowFunctionNode? {
        // current token is =>

        if (!checkNoLineTerminatorBeforeCurrent()) return reportUnexpectedToken()
        advance()

        val params = cover.toFormalParameters() ?: return null
        val body = parseConciseBody() ?: return null

        return ArrowFunctionNode(
            params,
            body,
            false,
            false,
            cover.range..body.range,
        )
    }
    @ReportsErrorDirectly
    private fun parseArrowFunction(
        isAsync: Boolean = false,
        isGenerator: Boolean = false,
        startRange: Range? = currToken.range,
    ): ArrowFunctionNode? {
        val nonNullStartRange = startRange ?: currToken.range
        val parameters =
            if (currToken.type == LEFT_PARENTHESIS) parseFormalParameters() ?: return null
            else {
                val paramName = parseIdentifier() ?: return null
                val param = paramName.toNonRest() ?: neverHappens()
                FormalParametersNode(listOf(param), param.range)
            }
        expect(ARROW) ?: return null
        val body = parseConciseBody() ?: return null
        return ArrowFunctionNode(parameters, body, isAsync, isGenerator, nonNullStartRange..body.range)
    }
    @Careful(false)
    private fun parseGeneratorArrowFunction(isAsync: Boolean = false, startRange: Range? = currToken.range): ArrowFunctionNode? {
        val genToken = takeIfMatchesKeyword(GEN)
        return parseArrowFunction(isAsync, genToken != null, startRange ?: genToken?.range)
    }
    @Careful(false)
    private fun parseAsyncArrowFunction(): ArrowFunctionNode? {
        val asyncToken = takeIfMatchesKeyword(ASYNC)
        return parseGeneratorArrowFunction(asyncToken != null, startRange=asyncToken?.range)
    }
    // </editor-fold>
    private fun ObjectLiteralNode.reportIfBinding(): ObjectLiteralNode? {
        val cover = elements.find { it is CoverInitializedNameNode }
        if (cover != null) return reportErrorMessage(SyntaxError.INVALID_COVER_INITIALIZED_NAME, cover.range)
        elements.forEach {
            it.reportIfBinding() ?: return null
        }
        return this
    }
    private fun ExpressionOrBindingPatternNode.reportIfBinding(): ExpressionNode? {
        return when (this) {
            is ObjectLiteralNode -> reportIfBinding()
            is ExpressionNode -> this
            else -> neverHappens()
        }
    }
    /**
     * Parses [AssignmentExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-AssignmentExpression).
     */
    @Careful(false)
    private fun parseAssignment(): ExpressionNode? =
        parseYield()
            ?: parseConditionalExpression()
                ?.let {
                    when (it) {
                        is IdentifierNode ->
                            if (currToken.type == ARROW) parseArrowFunctionWithoutParenthesis(it)
                            else it
                        is CoverParenthesizedExpressionAndArrowParameterListNode ->
                            if (currToken.type == ARROW) parseArrowFunctionByCover(it)
                            else {
                                val elements = it.elements.map {
                                    it.reportIfBinding() ?: return null
                                }
                                val innerExpr =
                                    if (elements.size == 1) elements.single()
                                    else SequenceExpressionNode(elements, elements.first().range..elements.last().range)
                                ParenthesizedExpressionNode(innerExpr, it.range)
                            }
                        else -> it
                    }
                }
                ?.let {
                    when (it) {
                        is UnaryExpressionNode, is BinaryExpressionNode, is ConditionalExpressionNode -> it
                        else -> { // it is LeftHandSideExpression
                            if (currToken.type !in ASSIGN..ASSIGN_MINUS) return@let it
                            val operation = BinaryOp.fromTokenType(currToken.type)
                            advance()
                            val right = parseAssignment() ?: return@let null
                            BinaryExpressionNode(it, right, operation)
                        }
                    }
                }
                ?: when {
                    currToken.isKeyword(ASYNC) -> parseAsyncArrowFunction()
                    currToken.isKeyword(GEN) -> parseGeneratorArrowFunction()
                    else -> ifHasNoError { reportUnexpectedToken() }
                }
    /**
     * Parses [Expression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-Expression).
     */
    @Careful(false)
    fun parseExpression(): ExpressionNode? {
        val expr = parseAssignment() ?: return null

        val exprs = mutableListOf(expr)
        var lastExpr = expr
        while (takeIfMatches(COMMA) != null) {
            lastExpr = parseAssignment() ?: return null
            exprs += lastExpr
        }

        return (
            if (exprs.size == 1) expr
            else SequenceExpressionNode(exprs, expr.range..lastExpr.range)
        )
    }
    // </editor-fold>
    @Careful(false)
    private fun parseInitializer(): InitializerNode? {
        val startRange = takeIfMatches(ASSIGN)?.range ?: return null
        val value = parseAssignment() ?: return null
        return InitializerNode(value, startRange)
    }
    @ReportsErrorDirectly
    private fun parseLexicalDeclaration(): LexicalDeclarationNode? {
        val kindToken = takeIfMatchesKeyword(VAR) ?: takeIfMatchesKeyword(LET) ?: return null

        val kind = LexicalDeclarationKind.valueOf(kindToken.rawContent.uppercase())
        val startRange = currToken.range
        val binding = parseIdentifier() ?: parseBindingPattern() ?: return reportUnexpectedToken()
        val value = parseInitializer()?.value ?: return (
            if (kind == LexicalDeclarationKind.LET || binding is BindingPatternNode) reportErrorMessage(
                SyntaxError.DECLARATION_MISSING_INITIALIZER,
                binding.range,
            )
            else LexicalDeclarationNode(kind, binding, null, startRange)
        )
        return LexicalDeclarationNode(kind, binding, value, startRange)
    }
    @Careful(false)
    private fun parseMethod(name: ObjectLiteralKeyNode, isAsync: Boolean, isGenerator: Boolean, startRange: Range): ObjectMethodNode? {
        val parameters = parseFormalParameters() ?: return null
        val body = parseBlockStatement() ?: return null
        return ObjectMethodNode(name, parameters, body, isAsync, isGenerator, startRange)
    }
    @Careful(false)
    private fun parseGeneratorMethod(givenName: ObjectLiteralKeyNode?, isAsync: Boolean): ObjectMethodNode? {
        val name = givenName ?: parsePropertyName() ?: return null
        if (name is IdentifierNode && name.isKeyword(GEN)) {
            val actualName = parsePropertyName() ?: return parseMethod(name, isAsync, false, name.range)
            return parseMethod(actualName, isAsync, true, name.range)
        }
        return parseMethod(name, isAsync, false, name.range)
    }
    @Careful(false)
    private fun parseAsyncMethod(givenName: ObjectLiteralKeyNode? = null): ObjectMethodNode? {
        val name = givenName ?: parsePropertyName() ?: return null
        if (name is IdentifierNode && name.isKeyword(ASYNC)) {
            val actualName = parsePropertyName() ?: return parseMethod(name, false, false, name.range)
            return parseGeneratorMethod(actualName, true)
        }
        return parseGeneratorMethod(name, false)
    }
    @ReportsErrorDirectly
    private fun parseMethodLikeObjectElement(givenName: ObjectLiteralKeyNode? = null): ObjectElementNode? {
        val name = givenName ?: parsePropertyName() ?: return null
        return (
            if (name is IdentifierNode) when {
                name.isKeyword(ASYNC) -> return parseAsyncMethod(name)
                name.isKeyword(GEN) -> return parseGeneratorMethod(name, false)
                currToken.type == LEFT_PARENTHESIS -> parseMethod(name, false, false, name.range)
                else -> {
                    val isGetter = name.isKeyword(GET)
                    val isSetter = name.isKeyword(SET)
                    if (!isGetter && !isSetter) return null
                    val actualName = parsePropertyName() ?: return null
                    val method = parseMethod(actualName, false, false, actualName.range) ?: return null
                    val params = method.parameters
                    when {
                        isGetter -> {
                            if (params.elements.isNotEmpty()) return reportErrorMessage(SyntaxError.BAD_GETTER_ARITY, params.range)
                            ObjectGetterNode(actualName, method.body, name.range)
                        }
                        isSetter -> {
                            val param = params.elements.singleOrNull() ?: return reportErrorMessage(SyntaxError.BAD_SETTER_ARITY, params.range)
                            if (param !is NonRestNode) return reportErrorMessage(SyntaxError.BAD_SETTER_REST_PARAMETER, params.range)
                            ObjectSetterNode(actualName, param, method.body, name.range)
                        }
                        else -> neverHappens()
                    }
                }
            }
            else parseMethod(name, false, false, name.range)
        )
    }
    @Careful(false)
    private fun parseMethodLikeClassElement(): ClassElementNode? {
        val staticToken = takeIfMatchesKeyword(STATIC)
        val isStatic = staticToken != null
        val elem = parseMethodLikeObjectElement() ?: return null
        val startRange = staticToken?.range ?: elem.range
        return elem.run {
            when (this) {
                is ObjectGetterNode -> ClassGetterNode(name, body, isStatic, startRange)
                is ObjectSetterNode -> ClassSetterNode(name, parameter, body, isStatic, startRange)
                is ObjectMethodNode -> ClassMethodNode(name, parameters, body, isAsync, isGenerator, isStatic, startRange)
                else -> neverHappens()
            }
        }
    }
    @Careful(false)
    private fun parseClassElement(): ClassElementNode? {
        return parseMethodLikeClassElement() // temp
    }
    @ReportsErrorDirectly
    private fun parseClassTail(): ClassTailNode? {
        val extendsTokenRange = takeIfMatchesKeyword(EXTENDS)?.range
        val parent =
            if (extendsTokenRange != null) parseLeftHandSideExpression()
            else null
        val leftBraceTokenRange = expect(LEFT_BRACE)?.range ?: return null
        val elements = mutableListOf<ClassElementNode>()
        while (currToken.type != RIGHT_BRACE) elements += parseClassElement() ?: return null
        val endRange = expect(RIGHT_BRACE)?.range ?: return null
        return ClassTailNode(parent, elements.toList(), (extendsTokenRange ?: leftBraceTokenRange)..endRange)
    }
    @Careful(false)
    private fun parseClassDeclaration(): DeclarationNode? {
        val startRange = takeIfMatchesKeyword(CLASS)?.range ?: return null
        val name = parseIdentifier() ?: return null
        val tail = parseClassTail() ?: return null
        return ClassDeclarationNode(name, tail.parent, tail.elements, startRange..tail.range)
    }
    @Careful(false)
    private fun parseDeclaration() =
        parseLexicalDeclaration() ?: parseClassDeclaration()
    @ReportsErrorDirectly
    private fun parseIf(): IfNode? {
        val startRange = expectKeyword(IF)?.range ?: return null

        expect(LEFT_PARENTHESIS) ?: return null
        val test = parseExpression() ?: return null
        expect(RIGHT_PARENTHESIS) ?: return null
        val body = parseStatement() ?: return null

        return IfNode(test, body, startRange)
    }
    @ReportsErrorDirectly
    private fun parseBlockStatement(): BlockStatementNode? {
        val statements = mutableListOf<StatementNode>()

        val startRange = expect(LEFT_BRACE)?.range ?: return null
        while (currToken.type != RIGHT_BRACE) statements += parseStatement() ?: return null
        val endRange = expect(RIGHT_BRACE)?.range ?: return null

        return BlockStatementNode(statements, startRange..endRange)
    }
    @Careful(false)
    private fun parseExpressionStatement(): ExpressionStatementNode? {
        val expr = parseExpression() ?: return null
        takeIfMatches(SEMICOLON)
        return ExpressionStatementNode(expr)
    }
    private fun parseStatement(
        allowModuleItem: Boolean = false,
        allowDeclaration: Boolean = true,
    ): StatementNode? {
        return when (currToken.type) {
            IDENTIFIER -> when {
                currToken.isKeyword(IF, true) -> parseIf()
                else -> parseExpressionStatement()
            }
            LEFT_BRACE -> parseBlockStatement()
            SEMICOLON -> EmptyStatementNode(advance().range)
            else -> parseExpressionStatement()
        }
    }
    private fun parseModuleItem() =
        parseStatement(allowModuleItem=true)
    @ReportsErrorDirectly
    fun parseProgram(): ProgramNode? {
        val statements = mutableListOf<StatementNode>()

        // TODO: Strict ASI behavior
        while (currToken.type != EOS) {
            if (!isLastStatementTerminated) return reportUnexpectedToken()
            isLastStatementTerminated = false
            val statement = parseModuleItem() ?: return null
            statements += statement
            isLastStatementTerminated = isLastStatementTerminated || currToken.isPrevLineTerminator
        }

        return ProgramNode(statements)
    }
}

private fun IdentifierOrBindingPatternNode.wrapNonRest() =
    NonRestNode(this, null)

private fun <T, R> Iterable<T>.foldElvis(operation: (T) -> R?) =
    fold(null as R?) { acc, it -> acc ?: operation(it) }

private fun IdentifierNode.isKeyword(keyword: ReservedWord) =
    value == keyword.value
private fun Token.isKeyword(keyword: ReservedWord, verifiedTokenType: Boolean = false) =
    (verifiedTokenType || type == IDENTIFIER) && rawContent == keyword.value

private fun neverHappens(): Nothing =
    throw Error("This can never happen")
