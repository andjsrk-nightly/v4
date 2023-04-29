package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.BinaryOperationType as BinaryOp
import io.github.andjsrk.v4.WasSuccessful
import io.github.andjsrk.v4.error.Error
import io.github.andjsrk.v4.error.SyntaxError
import io.github.andjsrk.v4.parse.Keyword.*
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
    private fun takeIfMatchesKeyword(keyword: Keyword) =
        if (currToken.isKeyword(keyword)) advance()
        else null
    private fun expect(tokenType: TokenType, check: (Token) -> Boolean = { true }) =
        if (currToken.type == tokenType && check(currToken)) advance()
        else reportUnexpectedToken()
    private fun expectKeyword(keyword: Keyword) =
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
    private fun checkNoLineTerminatorBeforeCurrent() =
        currToken.not { isPrevLineTerminator }
    // <editor-fold desc="primary expressions">
    /**
     * Parses [Identifier](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-Identifier).
     */
    @Careful
    private fun parseIdentifier(): IdentifierNode? {
        if (currToken.type != IDENTIFIER) return null
        if (Keyword.values().any { currToken.isKeyword(it) }) return null

        return IdentifierNode(currToken).alsoAdvance()
    }
    /**
     * Parses [IdentifierName](https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#prod-IdentifierName).
     */
    @Careful
    private fun parseIdentifierName() =
        takeIfMatches(IDENTIFIER)?.let(::IdentifierNode)
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
            IDENTIFIER -> when (currToken.rawContent) {
                "null" -> NullLiteralNode(currToken)
                "true", "false" -> BooleanLiteralNode(currToken)
                else -> null
            }
                ?.alsoAdvance()
            else -> null
        }
    @ReportsErrorDirectly
    private fun parseArrayElement(): MaybeSpreadNode? {
        val ellipsisToken = takeIfMatches(ELLIPSIS)
        val expr = parseAssignmentExpression() ?: return reportUnexpectedToken()

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

        val leftBracketTokenRange = expect(LEFT_BRACKET)?.range ?: return null
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
    private fun parseComputedPropertyName(): ComputedObjectKeyNode? {
        val startRange = takeIfMatches(LEFT_BRACKET)?.range ?: return null
        val expression = parseExpression() ?: return null
        val endRange = expect(RIGHT_BRACKET)?.range ?: return null

        return ComputedObjectKeyNode(expression, startRange..endRange)
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
    @Careful(completely=false)
    private fun parsePropertyName() =
        parseLiteralPropertyName() ?: parseComputedPropertyName()
    /**
     * Parses [PropertyDefinition](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-PropertyDefinition).
     */
    @ReportsErrorDirectly
    private fun parsePropertyDefinition(allowCoverInitializedName: Boolean = false): MaybeSpreadNode? {
        return when (currToken.type) {
            ELLIPSIS -> {
                val spreadToken = advance()
                val expression = parseAssignmentExpression() ?: return null // temp
                SpreadNode(expression, spreadToken.range..expression.range)
            }
            IDENTIFIER -> {
                val identifier = parseIdentifier()
                val propertyName = identifier ?: parsePropertyName() ?: return reportUnexpectedToken()
                NonSpreadNode(
                    when (currToken.type) {
                        COLON -> { // { a: b }
                            advance() // skip colon
                            val value = parseAssignmentExpression() ?: return null
                            PropertyNode(propertyName, value)
                        }
                        ASSIGN -> { // CoverInitializedName
                            if (identifier == null) return reportUnexpectedToken()
                            advance()
                            val default = parseAssignmentExpression() ?: return null
                            if (!allowCoverInitializedName) return reportErrorMessage(
                                SyntaxError.INVALID_COVER_INITIALIZED_NAME,
                                identifier.range..default.range,
                            )
                            CoverInitializedNameNode(identifier, default)
                        }
                        else -> {
                            if (identifier == null) TODO("Method definition is not supported yet")
                            PropertyShorthandNode(identifier)
                        }
                    }
                )
            }
            else -> reportUnexpectedToken()
        }
    }
    /**
     * Parses [ObjectLiteral](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ObjectLiteral).
     */
    @ReportsErrorDirectly
    private fun parseObjectLiteral(allowDestructuring: Boolean = false): ObjectLiteralNode? {
        val elements = mutableListOf<MaybeSpreadNode>()

        val leftBraceTokenRange = expect(LEFT_BRACE)?.range ?: return null
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
        val `as` = parseBindingIdentifier()
            ?: lazy { parseBindingPattern() }.takeIf { allowBindingPattern }?.value
            ?: return reportUnexpectedToken()
        when (currToken.type) {
            COMMA -> return reportErrorMessage(SyntaxError.ELEMENT_AFTER_REST)
            ASSIGN -> return reportErrorMessage(SyntaxError.REST_DEFAULT_INITIALIZER)
            else -> {}
        }
        return RestNode(`as`, restTokenRange..`as`.range)
    }
    /**
     * Parses [BindingElement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-BindingElement).
     */
    @ReportsErrorDirectly
    private fun parseBindingElement(): NonRestNode? {
        val `as` = parseBindingIdentifier() ?: parseBindingPattern() ?: return reportErrorMessage(SyntaxError.INVALID_DESTRUCTURING_TARGET)
        takeIfMatches(ASSIGN) ?: return NonRestNode(`as`, null)
        val default = parseAssignmentExpression() ?: return null
        return NonRestNode(`as`, default)
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
    @Careful(completely=false)
    private fun parseBindingProperty(): NonRestObjectPropertyNode? {
        // PropertyName contains BindingIdentifier, so it is not need to parse BindingIdentifier separately
        val left = parsePropertyName() ?: return null
        return when (currToken.type) {
            COLON -> {
                advance()
                val bindingElement = parseBindingElement() ?: return null
                return NonRestObjectPropertyNode(left, bindingElement.`as`, bindingElement.default)
            }
            ASSIGN -> {
                advance()
                val default = parseAssignmentExpression() ?: return null
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
    @Careful(completely=false)
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

        val items = mutableListOf<ExpressionOrBindingElementNode>()
        var skippedComma = true
        while (currToken.type != RIGHT_PARENTHESIS) {
            if (!skippedComma) return reportUnexpectedToken()
            if (currToken.type == ELLIPSIS) {
                items += parseBindingRestElement() ?: return null
                break
            } else {
                items += when (currToken.type) {
                    LEFT_BRACE -> parseObjectLiteral(true)
                    else -> parseAssignmentExpression()
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
    private fun parseArgument(): MaybeSpreadNode? {
        if (currToken.type == ELLIPSIS) {
            val spreadTokenRange = advance().range
            val expr = parseAssignmentExpression() ?: return null
            return SpreadNode(expr, spreadTokenRange..expr.range)
        }
        val expr = parseAssignmentExpression() ?: return null
        return NonSpreadNode(expr)
    }
    /**
     * Parses [Arguments](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-Arguments).
     */
    @ReportsErrorDirectly
    private fun parseArguments(call: FixedCalleeCallNode.Unsealed): List<MaybeSpreadNode>? {
        val args = mutableListOf<MaybeSpreadNode>()

        expect(LEFT_PARENTHESIS) ?: return null
        var skippedComma = true
        while (currToken.type != RIGHT_PARENTHESIS) {
            if (!skippedComma) return reportUnexpectedToken()
            args += parseArgument() ?: return null
            skippedComma = skip(COMMA)
        }
        call.endRange = expect(RIGHT_PARENTHESIS)?.range ?: return null

        return args.toList()
    }
    /**
     * Parses [MemberExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-MemberExpression).
     * Returns [NewExpressionNode] or optional chained [NormalCallNode] if possible.
     */
    @ReportsErrorDirectly
    private tailrec fun parseMemberExpression(`object`: ExpressionNode?, new: NewExpressionNode.Unsealed? = null): ExpressionNode? {
        val parsingNewExpression = new != null
        if (`object` == null) { // base case
            val primary = parsePrimaryExpression() ?: return null
            return parseMemberExpression(primary, new)
        }

        val member = MemberExpressionNode.Unsealed()
        member.`object` = `object`
        val questionDotToken = takeIfMatches(QUESTION_DOT)
        member.isOptionalChain = questionDotToken != null

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
                if (currToken.type == LEFT_PARENTHESIS) {
                    if (parsingNewExpression) {
                        requireNotNull(new)
                        new.callee = `object`
                        new.arguments += parseArguments(new) ?: return null
                        return new.toSealed()
                    } else return parseCall(`object`, member.isOptionalChain)
                }

                // if it is neither member access nor call, it is an unexpected token
                if (member.isOptionalChain) return reportUnexpectedToken()

                return `object`
            }
        }
        return parseMemberExpression(member.toSealed(), new)
    }
    /**
     * Parses [`new` expression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-MemberExpression).
     */
    private tailrec fun parseNewExpression(new: NewExpressionNode.Unsealed? = null): ExpressionNode? {
        val isCurrTokenNew = currToken.isKeyword(NEW)

        return (
            if (!isCurrTokenNew) parseMemberExpression(null, new) ?: parseCall(null)
            else {
                val freshNew = NewExpressionNode.Unsealed()
                freshNew.startRange = advance().range
                return parseNewExpression(freshNew)
            }
        )
    }
    /**
     * Parses [SuperCall](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-SuperCall).
     */
    @ReportsErrorDirectly
    private fun parseSuperCall(): SuperCallNode? {
        val superCall = SuperCallNode.Unsealed()

        val superToken = takeIfMatchesKeyword(SUPER) ?: return null
        superCall.superNode = SuperNode(superToken.range)
        takeIfMatches(LEFT_PARENTHESIS) ?: return reportErrorMessage(SyntaxError.UNEXPECTED_SUPER, superToken.range)
        superCall.arguments += parseArguments(superCall) ?: return null
        expect(RIGHT_PARENTHESIS) ?: return null

        return superCall.toSealed()
    }
    /**
     * Parses [ImportCall](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ImportCall).
     */
    @ReportsErrorDirectly
    private fun parseImportCall(): ImportCallNode? {
        val importCall = ImportCallNode.Unsealed()

        val importToken = takeIfMatchesKeyword(IMPORT) ?: return null
        importCall.importNode = ImportNode(importToken.range)
        expect(LEFT_PARENTHESIS) ?: return null
        importCall.pathSpecifier = parseExpression() ?: return null
        importCall.endRange = expect(RIGHT_PARENTHESIS)?.range ?: return null

        return importCall.toSealed()
    }
    /**
     * Parses [CallExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-CallExpression).
     */
    private fun parseCall(callee: ExpressionNode?, isOptionalChain: Boolean = false): ExpressionNode? {
        return (
            if (callee == null)
                when {
                    currToken.isKeyword(SUPER) -> parseSuperCall()
                    currToken.isKeyword(IMPORT) -> parseImportCall()
                    else -> null
                }
            else {
                val call = NormalCallNode.Unsealed()
                call.callee = callee
                call.isOptionalChain = isOptionalChain
                if (currToken.type != LEFT_PARENTHESIS) {
                    // if it is optional chain, it will be handled by parseMemberExpression
                    return callee
                }
                call.arguments += parseArguments(call) ?: return null
                return parseMemberExpression(call.toSealed())
            }
        )
    }
    /**
     * Parses [LeftHandSideExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-LeftHandSideExpression).
     */
    private fun parseLeftHandSideExpression() =
        parseNewExpression()
    /**
     * Parses [UpdateExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-UpdateExpression).
     */
    private fun parseUpdateExpression(): ExpressionNode? {
        return when (currToken.type) {
            INCREMENT, DECREMENT -> {
                val token = advance()
                val leftHandSideExpr = parseLeftHandSideExpression() ?: return null
                UpdateExpressionNode(
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
                    UpdateExpressionNode(
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
    private fun parseUnaryExpression(): ExpressionNode? {
        val operation = when {
            currToken.isKeyword(AWAIT) -> UnaryOperationType.AWAIT
            currToken.isKeyword(VOID) -> UnaryOperationType.VOID
            currToken.isKeyword(TYPEOF) -> UnaryOperationType.TYPEOF
            else -> when (currToken.type) {
                MINUS -> UnaryOperationType.MINUS
                NOT -> UnaryOperationType.NOT
                BITWISE_NOT -> UnaryOperationType.BITWISE_NOT
                else -> return parseUpdateExpression()
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
    private fun parseExponentiationExpression(): ExpressionNode? {
        val expr = parseUnaryExpression() ?: return null
        if (currToken.type != EXPONENTIAL) return expr
        val exponentialToken = advance()
        if (expr is UnaryExpressionNode && expr !is UpdateExpressionNode) return reportErrorMessage(
            SyntaxError.UNEXPECTED_TOKEN_UNARY_EXPONENTIATION,
            expr.range..exponentialToken.range,
        )
        val exponentiationExpr = parseExponentiationExpression() ?: return null
        return BinaryExpressionNode(expr, exponentiationExpr, BinaryOp.EXPONENTIAL)
    }
    /**
     * Parses [MultiplicativeExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-MultiplicativeExpression).
     */
    private tailrec fun parseMultiplicativeExpression(left: BinaryExpressionNode? = null): ExpressionNode? {
        val exponentiationExpr = left ?: parseExponentiationExpression() ?: return null
        val operation = when (currToken.type) {
            MULTIPLY -> BinaryOp.MULTIPLY
            DIVIDE -> BinaryOp.DIVIDE
            MOD -> BinaryOp.MOD
            else -> return exponentiationExpr
        }
        advance()
        val right = parseExponentiationExpression() ?: return null
        return parseMultiplicativeExpression(BinaryExpressionNode(exponentiationExpr, right, operation))
    }
    /**
     * Parses [AddictiveExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-AdditiveExpression).
     */
    private tailrec fun parseAddictiveExpression(left: BinaryExpressionNode? = null): ExpressionNode? {
        val multiplicativeExpr = left ?: parseMultiplicativeExpression() ?: return null
        val operation = when (currToken.type) {
            PLUS -> BinaryOp.PLUS
            MINUS -> BinaryOp.MINUS
            else -> return multiplicativeExpr
        }
        advance()
        val right = parseMultiplicativeExpression() ?: return null
        return parseAddictiveExpression(BinaryExpressionNode(multiplicativeExpr, right, operation))
    }
    /**
     * Parses [ShiftExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ShiftExpression).
     */
    private tailrec fun parseShiftExpression(left: BinaryExpressionNode? = null): ExpressionNode? {
        val addictiveExpr = left ?: parseAddictiveExpression() ?: return null
        val operation = when (currToken.type) {
            SHL -> BinaryOp.SHL
            SAR -> BinaryOp.SAR
            SHR -> BinaryOp.SHR
            else -> return addictiveExpr
        }
        advance()
        val right = parseAddictiveExpression() ?: return null
        return parseShiftExpression(BinaryExpressionNode(addictiveExpr, right, operation))
    }
    /**
     * Parses [RelationalExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-RelationalExpression).
     */
    private tailrec fun parseRelationalExpression(left: BinaryExpressionNode? = null): ExpressionNode? {
        val shiftExpr = left ?: parseShiftExpression() ?: return null
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
        val right = parseShiftExpression() ?: return null
        return parseRelationalExpression(BinaryExpressionNode(shiftExpr, right, operation))
    }
    /**
     * Parses [EqualityExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-EqualityExpression).
     */
    private tailrec fun parseEqualityExpression(left: BinaryExpressionNode? = null): ExpressionNode? {
        val relationalExpr = left ?: parseRelationalExpression() ?: return null
        if (currToken.type !in EQ..NOT_EQ_STRICT) return relationalExpr
        val operation = BinaryOp.fromTokenType(currToken.type)
        advance()
        val right = parseRelationalExpression() ?: return null
        return parseEqualityExpression(BinaryExpressionNode(relationalExpr, right, operation))
    }
    private fun parseGeneralBinaryExpression(opTokenType: TokenType, parseInner: () -> ExpressionNode?): ExpressionNode? {
        tailrec fun parse(left: BinaryExpressionNode? = null): ExpressionNode? {
            val expr = left ?: parseInner() ?: return null
            takeIfMatches(opTokenType) ?: return expr
            val right = parseInner() ?: return null
            return parse(BinaryExpressionNode(expr, right, BinaryOp.fromTokenType(opTokenType)))
        }
        return parse()
    }
    /**
     * Parses [BitwiseANDExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-BitwiseANDExpression).
     */
    private fun parseBitwiseAndExpression() =
        parseGeneralBinaryExpression(BITWISE_AND, this::parseEqualityExpression)
    /**
     * Parses [BitwiseXORExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-BitwiseXORExpression).
     */
    private fun parseBitwiseXorExpression() =
        parseGeneralBinaryExpression(BITWISE_XOR, this::parseBitwiseAndExpression)
    /**
     * Parses [BitwiseORExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-BitwiseORExpression).
     */
    private fun parseBitwiseOrExpression() =
        parseGeneralBinaryExpression(BITWISE_OR, this::parseBitwiseXorExpression)
    /**
     * Parses [LogicalANDExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-LogicalANDExpression).
     */
    private fun parseLogicalAndExpression() =
        parseGeneralBinaryExpression(AND, this::parseBitwiseOrExpression)
    /**
     * Parses [LogicalORExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-LogicalORExpression).
     */
    private fun parseLogicalOrExpression() =
        parseGeneralBinaryExpression(OR, this::parseLogicalAndExpression)
    /**
     * Parses [CoalesceExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-CoalesceExpression).
     * Note that the method takes non-null parse result from [parseLogicalOrExpression].
     */
    @ReportsErrorDirectly
    private tailrec fun parseCoalesceExpression(left: ExpressionNode): ExpressionNode? {
        val coalesceToken = takeIfMatches(COALESCE) ?: return left
        if (left is BinaryExpressionNode && left.operation.isOneOf(BinaryOp.OR, BinaryOp.AND)) return reportUnexpectedToken(coalesceToken)
        val right = parseBitwiseOrExpression() ?: return null
        return parseCoalesceExpression(BinaryExpressionNode(left, right, BinaryOp.COALESCE))
    }
    /**
     * Parses [ShortCircuitExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ShortCircuitExpression).
     */
    private fun parseShortCircuitExpression() =
        parseLogicalOrExpression()
            ?.let(this::parseCoalesceExpression)
    /**
     * Parses [ConditionalExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ConditionalExpression).
     */
    @ReportsErrorDirectly
    private fun parseConditionalExpression(): ExpressionNode? {
        val shortCircuitExpr = parseShortCircuitExpression() ?: return null
        takeIfMatches(CONDITIONAL) ?: return shortCircuitExpr
        val consequent = parseAssignmentExpression() ?: return null
        expect(COLON) ?: return null
        val alternative = parseAssignmentExpression() ?: return null
        return ConditionalExpressionNode(shortCircuitExpr, consequent, alternative)
    }
    /**
     * Parses [YieldExpression](https://tc39.es/ecma262/multipage/ecmascript-language-functions-and-classes.html#prod-YieldExpression).
     */
    private fun parseYieldExpression(): YieldExpressionNode? {
        val yieldTokenRange = takeIfMatchesKeyword(YIELD)?.range ?: return null
        if (currToken.isPrevLineTerminator) return YieldExpressionNode(null, false, yieldTokenRange)
        val isDelegate = takeIfMatches(MULTIPLY) != null
        val expr = parseAssignmentExpression() ?: return null
        return YieldExpressionNode(expr, isDelegate, yieldTokenRange..expr.range)
    }
    /**
     * Parses [ConciseBody](https://tc39.es/ecma262/multipage/ecmascript-language-functions-and-classes.html#prod-ConciseBody).
     */
    private fun parseConciseBody() =
        if (currToken.type == LEFT_BRACE) parseBlockStatement()
        else parseAssignmentExpression()
    @ReportsErrorDirectly
    private fun parseArrowFunctionWithoutParenthesis(parameter: IdentifierNode): ArrowFunctionNode? {
        // current token is =>

        if (!checkNoLineTerminatorBeforeCurrent()) return reportUnexpectedToken()
        advance()

        val body = parseConciseBody() ?: return null

        return ArrowFunctionNode(
            listOf(parameter.toNonRest() ?: neverHappens()),
            body,
            false,
            false,
            parameter.range..body.range,
        )
    }
    @ReportsErrorDirectly
    private fun Node.toNonRestNodeRight(carefully: Boolean = false): IdentifierOrBindingElementNode? =
        when (this) {
            is IdentifierNode -> this
            is CollectionLiteralNode -> this.toBindingPattern()
            else -> {
                if (!carefully) reportErrorMessage(
                    SyntaxError.INVALID_DESTRUCTURING_TARGET,
                    range,
                )
                null
            }
        }
    private fun Node.toNonRest(carefully: Boolean = false) =
        this.toNonRestNodeRight(carefully)?.wrapNonRest()
    private fun MaybeSpreadNode.toMaybeRest(): MaybeRestNode? {
        return when (this) {
            is NonSpreadNode -> when (val expr = expression) {
                is IdentifierNode, is CollectionLiteralNode -> expr.toNonRest(true)
                is BinaryExpressionNode -> {
                    val left = expr.left.toNonRestNodeRight(true) ?: return null
                    NonRestNode(left, expr.right)
                }
                else -> null
            }
            is SpreadNode -> RestNode(expression, range)
        }
    }
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
                    is NonSpreadNode -> when (val expr = it.expression) {
                        is CoverInitializedNameNode -> NonRestObjectPropertyNode(expr.key, expr.key, expr.default)
                        is PropertyShorthandNode -> {
                            if (expr.name !is IdentifierNode) return reportErrorMessage(SyntaxError.INVALID_REST_BINDING_PATTERN)
                            NonRestObjectPropertyNode(expr.name, expr.name, null)
                        }
                        is PropertyNode -> when (val value = expr.value) {
                            is BinaryExpressionNode -> NonRestObjectPropertyNode(expr.key, value.left, value.right)
                            else -> NonRestObjectPropertyNode(
                                expr.key,
                                value.toNonRestNodeRight() ?: return null,
                                null,
                            )
                        }
                        else -> neverHappens()
                    }
                }
            },
            range,
        )
    }
    private fun CollectionLiteralNode.toBindingPattern() =
        when (this) {
            is ArrayLiteralNode -> this.toArrayBindingPattern()
            is ObjectLiteralNode -> this.toObjectBindingPattern()
        }
    private val Node.invalidDestructuringRange: Range? get() =
        when (this) {
            is IdentifierNode,
            is RestNode,
                -> null
            is CollectionLiteralNode ->
                elements.foldElvis { it.invalidDestructuringRange }
            is BinaryExpressionNode ->
                if (operation == BinaryOp.ASSIGN && left is IdentifierNode) null
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
        items.foldElvis { it.invalidDestructuringRange }
    @ReportsErrorDirectly
    private fun CoverParenthesizedExpressionAndArrowParameterListNode.toArrowParameters(): List<MaybeRestNode?>? {
        val invalidRange = findInvalidDestructuringRange()
        if (invalidRange != null) {
            if (!hasError) reportErrorMessage(SyntaxError.INVALID_DESTRUCTURING_TARGET, invalidRange)
            return null
        }

        return items.map {
            when (it) {
                is IdentifierNode -> it.wrapNonRest()
                is BinaryExpressionNode -> NonRestNode(it.left, it.right)
                is CollectionLiteralNode -> it.toBindingPattern()?.wrapNonRest()
                is RestNode -> it
                else -> reportErrorMessage(SyntaxError.INVALID_DESTRUCTURING_TARGET, it.range)
            }
        }
    }
    /**
     * Parses [ArrowFormalParameters](https://tc39.es/ecma262/multipage/ecmascript-language-functions-and-classes.html#prod-ArrowFormalParameters).
     */
    @ReportsErrorDirectly
    private fun parseArrowFormalParameters(): List<MaybeRestNode>? {
        expect(LEFT_PARENTHESIS) ?: return null
        val items = mutableListOf<MaybeRestNode>()
        var skippedComma = true
        while (currToken.type != RIGHT_PARENTHESIS) {
            if (!skippedComma) return reportUnexpectedToken()
            if (currToken.type == ELLIPSIS) {
                items += parseBindingRestElement() ?: return null
                break
            }
            items += parseBindingElement() ?: return null
            skippedComma = skip(COMMA)
        }
        expect(RIGHT_PARENTHESIS) ?: return null

        return items.toList()
    }
    @ReportsErrorDirectly
    private fun parseArrowFunctionByCover(cover: CoverParenthesizedExpressionAndArrowParameterListNode): ArrowFunctionNode? {
        // current token is =>

        if (!checkNoLineTerminatorBeforeCurrent()) return reportUnexpectedToken()
        advance()

        val params = cover.toArrowParameters() ?: return null
        val body = parseConciseBody() ?: return null

        return ArrowFunctionNode(
            params.map { it ?: return null },
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
            if (currToken.type == IDENTIFIER) {
                val paramName = parseIdentifier() ?: return null
                val param = paramName.toNonRest() ?: neverHappens()
                listOf(param)
            } else parseArrowFormalParameters() ?: return null
        expect(ARROW) ?: return null
        val body = parseConciseBody() ?: return null
        return ArrowFunctionNode(parameters, body, isAsync, isGenerator, nonNullStartRange..body.range)
    }
    private fun parseGeneratorArrowFunction(isAsync: Boolean = false, startRange: Range? = currToken.range): ArrowFunctionNode? {
        val starToken = takeIfMatches(MULTIPLY)
        return parseArrowFunction(isAsync, starToken != null, startRange ?: starToken?.range)
    }
    private fun parseAsyncArrowFunction(): ArrowFunctionNode? {
        val asyncToken = takeIfMatchesKeyword(ASYNC)
        return parseGeneratorArrowFunction(asyncToken != null, startRange=asyncToken?.range)
    }
    /**
     * Parses [AssignmentExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-AssignmentExpression).
     */
    private fun parseAssignmentExpression(): ExpressionNode? =
        parseYieldExpression()
            ?: parseConditionalExpression()?.let {
                when (it) {
                    is IdentifierNode ->
                        if (currToken.type == ARROW) parseArrowFunctionWithoutParenthesis(it)
                        else it
                    is CoverParenthesizedExpressionAndArrowParameterListNode ->
                        if (currToken.type == ARROW) parseArrowFunctionByCover(it)
                        else {
                            TODO("ParenthesizedExpression is not supported yet")
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
                        val right = parseAssignmentExpression() ?: return@let null
                        BinaryExpressionNode(it, right, operation)
                    }
                }
            }
            ?: when {
                currToken.isKeyword(ASYNC) -> parseAsyncArrowFunction()
                currToken.type == MULTIPLY -> parseGeneratorArrowFunction()
                else -> null
            }
    /**
     * Parses [Expression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-Expression).
     */
    private fun parseExpression(): ExpressionNode? {
        val expr = parseAssignmentExpression() ?: return null

        val exprs = mutableListOf(expr)
        var lastExpr = expr
        while (takeIfMatches(COMMA) != null) {
            lastExpr = parseAssignmentExpression() ?: return null
            exprs += lastExpr
        }

        return (
            if (exprs.size == 1) expr
            else SequenceExpressionNode(exprs, expr.range..lastExpr.range)
        )
    }
    @ReportsErrorDirectly
    private fun parseIfStatement(): IfStatementNode? {
        val `if` = IfStatementNode.Unsealed()
        `if`.startRange = expectKeyword(IF)?.range ?: return null

        expect(LEFT_PARENTHESIS) ?: return null
        `if`.test = parseExpression() ?: return null
        expect(RIGHT_PARENTHESIS) ?: return null
        `if`.body = parseStatement() ?: return null

        return `if`.toSealed()
    }
    @ReportsErrorDirectly
    private fun parseBlockStatement(): BlockStatementNode? {
        val statements = mutableListOf<StatementNode>()

        val startRange = expect(LEFT_BRACE)?.range ?: return null
        while (currToken.type != RIGHT_BRACE) statements += parseStatement() ?: return null
        val endRange = expect(RIGHT_BRACE)?.range ?: return null

        return BlockStatementNode(statements, startRange..endRange)
    }
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
            IDENTIFIER -> when (currToken.rawContent) {
                "if" -> parseIfStatement()
                else -> parseExpressionStatement()
            }
            LEFT_BRACE -> parseBlockStatement()
            else -> parseExpressionStatement()
        }
    }
    private fun parseModuleItem() =
        parseStatement(allowModuleItem=true)
    fun parseProgram(): ProgramNode? {
        val statements = mutableListOf<StatementNode>()

        // TODO: Strict ASI behavior
        var isLastNodeExpression = false
        while (currToken.type != EOS) {
            if (isLastNodeExpression && currToken.not { isPrevLineTerminator }) return reportUnexpectedToken()
            val statement = parseModuleItem() ?: return null
            isLastNodeExpression = statement is ExpressionStatementNode
            statements += statement
        }

        return ProgramNode(statements)
    }
}

private fun IdentifierOrBindingElementNode.wrapNonRest() =
    NonRestNode(this, null)

private fun <T, R> Iterable<T>.foldElvis(operation: (T) -> R?) =
    fold(null as R?) { acc, it -> acc ?: operation(it) }

private fun Token.isKeyword(keyword: Keyword) =
    type == IDENTIFIER && rawContent == keyword.value

private fun neverHappens(): Nothing =
    throw Error("This can never happen")
