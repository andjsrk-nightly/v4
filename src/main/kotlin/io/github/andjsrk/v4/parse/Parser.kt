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
import io.github.andjsrk.v4.parse.node.ThisReferenceNode
import io.github.andjsrk.v4.parse.node.literal.*
import io.github.andjsrk.v4.parse.node.literal.`object`.*
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
    private fun reportErrorMessage(kind: Error, range: Range, vararg args: String): Nothing? {
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
    private fun parseArrayElement(): CommaSeparatedElementNode? {
        val ellipsisToken = takeIfMatches(ELLIPSIS)
        val isSpread = ellipsisToken != null
        val expr = parseAssignmentExpression() ?: return null
        val range =
            if (isSpread) ellipsisToken!!.range..expr.range
            else expr.range
        return CommaSeparatedElementNode(expr, isSpread, range)
    }
    /**
     * Parses [ArrayLiteral](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ArrayLiteral).
     */
    private fun parseArrayLiteral(): ArrayLiteralNode? {
        val elements = mutableListOf<CommaSeparatedElementNode>()

        val leftBracketTokenRange = expect(LEFT_BRACKET)?.range ?: return null
        // allow trailing comma, but disallow sparse array
        var skippedComma = true
        while (currToken.type != RIGHT_BRACKET) {
            if (!skippedComma || currToken.type == COMMA) return reportUnexpectedToken()
            elements += parseArrayElement() ?: return null
            skippedComma = skip(COMMA)
        }
        val rightBracketTokenRange = expect(RIGHT_BRACKET)?.range ?: return null

        return ArrayLiteralNode(elements.toList(), leftBracketTokenRange..rightBracketTokenRange)
    }
    /**
     * Parses [ComputedPropertyName](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ComputedPropertyName).
     */
    private fun parseComputedPropertyName(property: PropertyNode.Unsealed): ExpressionNode? {
        property.startRange = takeIfMatches(LEFT_BRACKET)?.range ?: return null
        val expression = parseExpression() ?: return null
        expect(RIGHT_BRACKET) ?: return null

        property.isComputed = true

        return expression
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
    private fun parsePropertyName(property: PropertyNode.Unsealed) =
        parseLiteralPropertyName() ?: parseComputedPropertyName(property)
    /**
     * Parses [PropertyDefinition](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-PropertyDefinition).
     */
    private fun parsePropertyDefinition(): ObjectElementNode? {
        return when (currToken.type) {
            ELLIPSIS -> {
                val spreadToken = advance()
                val expression = parseExpression() ?: return null // temp
                CommaSeparatedElementNode(expression, true, spreadToken.range..expression.range)
            }
            IDENTIFIER -> {
                val identifier = parseIdentifier()
                val property = PropertyNode.Unsealed()
                val propertyName = identifier ?: parsePropertyName(property)
                when (currToken.type) {
                    COLON -> { // { a: b }
                        // we must check whether the key is valid first
                        if (propertyName == null) return reportUnexpectedToken()

                        property.key = propertyName
                        advance() // skip colon
                        property.value = parseExpression() ?: return null // temp
                        property.startRange = property.key.range
                        property.toSealed()
                    }
                    else -> {
                        if (identifier == null) TODO("Method definition is not supported yet")
                        PropertyShorthandNode(identifier)
                    }
                }
            }
            else -> reportUnexpectedToken()
        }
    }
    /**
     * Parses [ObjectLiteral](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ObjectLiteral).
     */
    private fun parseObjectLiteral(): ObjectLiteralNode? {
        val elements = mutableListOf<ObjectElementNode>()

        val leftBraceTokenRange = expect(LEFT_BRACE)?.range ?: return null
        var skippedComma = true
        while (currToken.type != RIGHT_BRACE) {
            if (!skippedComma || currToken.type == COMMA) return reportUnexpectedToken()
            val element = parsePropertyDefinition() ?: return null
            elements += element
            skippedComma = skip(COMMA)
        }
        val rightBraceTokenRange = expect(RIGHT_BRACE)?.range ?: return null

        return ObjectLiteralNode(elements.toList(), leftBraceTokenRange..rightBraceTokenRange)
    }
    private fun parseThisReference(): ThisReferenceNode? =
        takeIfMatchesKeyword(THIS)?.let {
            ThisReferenceNode(it.range)
        }
    private fun parseParenthesizedExpression(): ExpressionNode? {
        val leftParenRange = takeIfMatches(LEFT_PARENTHESIS)?.range ?: return null
        val exprs = mutableListOf<ExpressionNode>()
        var skippedComma = false
        while (currToken.type != RIGHT_PARENTHESIS) {
            // prevent double comma
            if (skippedComma && currToken.type == COMMA) return reportUnexpectedToken()
            exprs += parseExpression() ?: return null
            skippedComma = skip(COMMA)
        }
        if (skippedComma) return reportUnexpectedToken()
        val rightParenRange = expect(RIGHT_PARENTHESIS)?.range ?: throw Error("This can never happen")

        val range = leftParenRange..rightParenRange

        return (
            exprs.singleOrNull()?.let { expr -> ParenthesizedExpressionNode(expr, range) }
                ?: SequenceExpressionNode(exprs.toList(), range)
        )
    }
    /**
     * Parses [PrimaryExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-PrimaryExpression).
     */
    private fun parsePrimaryExpression(): ExpressionNode? =
        parsePrimitiveLiteral() ?: parseIdentifier() ?: when (currToken.type) {
            IDENTIFIER -> // now there are only keywords except primitive literals
                parseThisReference() // temp
            LEFT_PARENTHESIS -> parseParenthesizedExpression()
            LEFT_BRACE -> parseObjectLiteral()
            LEFT_BRACKET -> parseArrayLiteral()
            else -> null
        }
    // </editor-fold>
    private fun parseArgument(): Argument? {
        if (currToken.type == ELLIPSIS) { // spread
            val spreadToken = advance()
            val expr = parseExpression() ?: return null
            return Argument(expr, true, spreadToken.range..expr.range)
        }
        val expr = parseExpression() ?: return null
        return Argument(expr, false, expr.range)
    }
    /**
     * Parses [Arguments](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-Arguments).
     */
    private fun parseArguments(call: FixedCalleeCallExpressionNode.Unsealed): Arguments? {
        val args = mutableListOf<Argument>()

        expect(LEFT_PARENTHESIS) ?: return null
        var skippedComma = true
        while (currToken.type != RIGHT_PARENTHESIS) {
            if (!skippedComma || currToken.type == COMMA) return reportUnexpectedToken()
            args += parseArgument() ?: return null
            skippedComma = skip(COMMA)
        }
        call.endRange = expect(RIGHT_PARENTHESIS)?.range ?: return null

        return args.toList()
    }
    /**
     * Parses [MemberExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-MemberExpression).
     * Returns [NewExpressionNode] or optional chained [NormalCallExpressionNode] if possible.
     */
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
                if (member.isOptionalChain.not()) return `object`
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
                    } else return parseCallExpression(`object`, member.isOptionalChain)
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
            if (!isCurrTokenNew) parseMemberExpression(null, new) ?: parseCallExpression(null)
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
    private fun parseCallExpression(callee: ExpressionNode?, isOptionalChain: Boolean = false): ExpressionNode? {
        return (
            if (callee == null)
                when {
                    currToken.isKeyword(SUPER) -> parseSuperCall()
                    currToken.isKeyword(IMPORT) -> parseImportCall()
                    else -> null
                }
            else {
                val call = NormalCallExpressionNode.Unsealed()
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
                if (currToken.afterLineTerminator.not() && currToken.type.isOneOf(INCREMENT, DECREMENT)) {
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
        val operation = when (currToken.type) {
            EQ -> BinaryOp.EQ
            NOT_EQ -> BinaryOp.NOT_EQ
            EQ_STRICT -> BinaryOp.EQ_STRICT
            NOT_EQ_STRICT -> BinaryOp.NOT_EQ_STRICT
            else -> return relationalExpr
        }
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
    private fun parseConditionalExpression(): ExpressionNode? {
        val shortCircuitExpr = parseShortCircuitExpression() ?: return null
        takeIfMatches(CONDITIONAL) ?: return shortCircuitExpr
        val consequent = parseAssignmentExpression() ?: return null
        expect(COLON) ?: return null
        val alternative = parseAssignmentExpression() ?: return null
        return ConditionalExpressionNode(shortCircuitExpr, consequent, alternative)
    }
    private fun parseYieldExpression(): YieldExpressionNode? {
        val yieldTokenRange = takeIfMatchesKeyword(YIELD)?.range ?: return null
        if (currToken.afterLineTerminator) return YieldExpressionNode(null, false, yieldTokenRange)
        val isDelegate = takeIfMatches(MULTIPLY) != null
        val expr = parseAssignmentExpression() ?: return null
        return YieldExpressionNode(expr, isDelegate, yieldTokenRange..expr.range)
    }
    /**
     * Parses [AssignmentExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-AssignmentExpression).
     */
    private fun parseAssignmentExpression(): ExpressionNode? {
        return parseYieldExpression() ?: parseConditionalExpression()
    }
    private fun parseExpression(): ExpressionNode? {
        return parseAssignmentExpression() // temp
    }
    private fun parseIfStatement(): IfStatementNode? {
        val `if` = IfStatementNode.Unsealed()
        `if`.startRange = expectKeyword(IF)?.range ?: return null

        expect(LEFT_PARENTHESIS) ?: return null
        `if`.test = parseExpression() ?: return null
        expect(RIGHT_PARENTHESIS) ?: return null
        `if`.body = parseStatement() ?: return null

        return `if`.toSealed()
    }
    private fun parseBlockStatement(): BlockStatementNode? {
        val block = BlockStatementNode.Unsealed()

        block.startRange = expect(LEFT_BRACE)?.range ?: return null
        while (currToken.type != RIGHT_BRACE) {
            block.statements += parseStatement() ?: return null
        }
        block.endRange = expect(RIGHT_BRACE)?.range ?: return null

        return block.toSealed()
    }
    private fun parseStatement(
        allowModuleItem: Boolean = false,
        allowDeclaration: Boolean = true,
    ): StatementNode? {
        return when (currToken.type) {
            IDENTIFIER -> when (currToken.rawContent) {
                "if" -> parseIfStatement()
                else -> parseExpression()?.let(::ExpressionStatementNode)
            }
            LEFT_BRACE -> parseBlockStatement()
            else -> parseExpression()?.let(::ExpressionStatementNode)
        }
    }
    private fun parseModuleItem() =
        parseStatement(allowModuleItem=true)
    fun parseProgram(): ProgramNode? {
        val statements = mutableListOf<StatementNode>()

        var isLastNodeExpression = false
        while (currToken.type != EOS) {
            if (isLastNodeExpression && currToken.afterLineTerminator.not()) return reportUnexpectedToken()
            val statement = parseModuleItem() ?: return null
            isLastNodeExpression = statement is ExpressionStatementNode
            statements += statement
        }

        return ProgramNode(statements)
    }
}

private fun Token.isKeyword(keyword: Keyword) =
    type == IDENTIFIER && rawContent == keyword.value
