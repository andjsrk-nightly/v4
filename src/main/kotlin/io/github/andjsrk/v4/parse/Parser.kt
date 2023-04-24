package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.*
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
    /**
     * Parses [ArrayLiteral](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ArrayLiteral).
     */
    private fun parseArrayLiteral(): ArrayLiteralNode? {
        val array = ArrayLiteralNode.Unsealed()

        array.startToken = expect(LEFT_BRACKET) ?: return null
        // allow trailing comma, but disallow sparse array
        var skippedComma = true
        while (currToken.type != RIGHT_BRACKET) {
            if (!skippedComma || currToken.type == COMMA) return reportUnexpectedToken()
            array.items += parseExpression() ?: return null
            skippedComma = skip(COMMA)
        }
        array.endToken = expect(RIGHT_BRACKET) ?: return null

        return array.toSealed()
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
                CommaSeparatedElementNode(expression, true, spreadToken.range until expression.range)
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
        val obj = ObjectLiteralNode.Unsealed()

        obj.startToken = expect(LEFT_BRACE) ?: return null
        var skippedComma = true
        while (currToken.type != RIGHT_BRACE) {
            if (!skippedComma || currToken.type == COMMA) return reportUnexpectedToken()
            val element = parsePropertyDefinition() ?: return null
            obj.elements += element
            skippedComma = skip(COMMA)
        }
        obj.endToken = expect(RIGHT_BRACE) ?: return null

        return obj.toSealed()
    }
    private fun parseThisReference(): ThisReferenceNode? =
        takeIfMatchesKeyword(THIS)?.let {
            ThisReferenceNode(it.range)
        }
    /**
     * Parses [PrimaryExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-PrimaryExpression).
     */
    private fun parsePrimaryExpression(): ExpressionNode? =
        parsePrimitiveLiteral() ?: parseIdentifier() ?: when (currToken.type) {
            IDENTIFIER -> // now there are only keywords except primitive literals
                parseThisReference() // temp
            LEFT_BRACKET -> parseArrayLiteral()
            LEFT_BRACE -> parseObjectLiteral()
            else -> null
        }
    // </editor-fold>
    private fun parseArgumentItem(): CommaSeparatedElementNode? {
        if (currToken.type == ELLIPSIS) { // spread
            val spreadToken = advance()
            val expr = parseExpression() ?: return null
            return CommaSeparatedElementNode(expr, true, spreadToken.range until expr.range)
        }
        val expr = parseExpression() ?: return null
        return CommaSeparatedElementNode(expr, false, expr.range)
    }
    /**
     * Parses [Arguments](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-Arguments).
     */
    private fun parseArguments(call: FixedCalleeCallExpressionNode.Unsealed): List<CommaSeparatedElementNode>? {
        val args = mutableListOf<CommaSeparatedElementNode>()

        expect(LEFT_PARENTHESIS) ?: return null
        var skippedComma = true
        while (currToken.type != RIGHT_PARENTHESIS) {
            if (!skippedComma || currToken.type == COMMA) return reportUnexpectedToken()
            args += parseArgumentItem() ?: return null
            skippedComma = skip(COMMA)
        }
        call.endRange = expect(RIGHT_PARENTHESIS)?.range ?: return null

        return args.toList()
    }
    /**
     * Parses [MemberExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-MemberExpression).
     * Returns [NewExpressionNode] or optional chained [NormalCallExpressionNode] if possible.
     */
    private fun parseMemberExpression(`object`: ExpressionNode?, new: NewExpressionNode.Unsealed? = null): ExpressionNode? {
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
    private fun parseNewExpression(new: NewExpressionNode.Unsealed? = null): ExpressionNode? {
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
                UnaryExpressionNode(
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
                    UnaryExpressionNode(
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
    private fun parseUnaryExpression(updateExpression: ExpressionNode? = null): ExpressionNode? {
        val operation = when {
            currToken.isKeyword(AWAIT) -> UnaryOperationType.AWAIT
            currToken.isKeyword(VOID) -> UnaryOperationType.VOID
            currToken.isKeyword(TYPEOF) -> UnaryOperationType.TYPEOF
            else -> when (currToken.type) {
                MINUS -> UnaryOperationType.MINUS
                NOT -> UnaryOperationType.NOT
                BITWISE_NOT -> UnaryOperationType.BITWISE_NOT
                else -> null
            }
        }
        val operationToken = operation?.let { advance() }
        val updateExpr = updateExpression ?: parseUpdateExpression() ?: return null

        if (operation == null) return updateExpr

        return UnaryExpressionNode(updateExpr, operation, operationToken!!.range)
    }
    /**
     * Parses [ExponentiationExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ExponentiationExpression).
     */
    private fun parseExponentiationExpression(): ExpressionNode? {
        val updateExpr = parseUpdateExpression() ?: return null
        if (currToken.type != EXPONENTIAL) return parseUnaryExpression(updateExpr)
        advance()
        val exponentiationExpr = parseExponentiationExpression() ?: return null
        return BinaryExpressionNode(updateExpr, exponentiationExpr, BinaryOperationType.EXPONENTIAL)
    }
    /**
     * Parses [MultiplicativeExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-MultiplicativeExpression).
     */
    private fun parseMultiplicativeExpression(left: BinaryExpressionNode? = null): ExpressionNode? {
        val exponentiationExpr = left ?: parseExponentiationExpression() ?: return null
        val operation = when (currToken.type) {
            MULTIPLY -> BinaryOperationType.MULTIPLY
            DIVIDE -> BinaryOperationType.DIVIDE
            MOD -> BinaryOperationType.MOD
            else -> return exponentiationExpr
        }
        val right = parseExponentiationExpression() ?: return null
        return parseMultiplicativeExpression(BinaryExpressionNode(exponentiationExpr, right, operation))
    }
    /**
     * Parses [AddictiveExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-AdditiveExpression).
     */
    private fun parseAddictiveExpression(left: BinaryExpressionNode? = null): ExpressionNode? {
        val multiplicativeExpr = left ?: parseMultiplicativeExpression() ?: return null
        val operation = when (currToken.type) {
            PLUS -> BinaryOperationType.PLUS
            MINUS -> BinaryOperationType.MINUS
            else -> return multiplicativeExpr
        }
        val right = parseMultiplicativeExpression() ?: return null
        return parseAddictiveExpression(BinaryExpressionNode(multiplicativeExpr, right, operation))
    }
    /**
     * Parses [ShiftExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ShiftExpression).
     */
    private fun parseShiftExpression(left: BinaryExpressionNode? = null): ExpressionNode? {
        val addictiveExpr = left ?: parseAddictiveExpression() ?: return null
        val operation = when (currToken.type) {
            SHL -> BinaryOperationType.SHL
            SAR -> BinaryOperationType.SAR
            SHR -> BinaryOperationType.SHR
            else -> return addictiveExpr
        }
        val right = parseAddictiveExpression() ?: return null
        return parseShiftExpression(BinaryExpressionNode(addictiveExpr, right, operation))
    }
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-RelationalExpression
    private fun parseRelationalExpression(): BinaryExpressionNode? {
        TODO()
    }
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ConditionalExpression
    private fun parseConditionalExpression() {
        TODO()
    }
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-AssignmentExpression
    private fun parseAssignmentExpression() {
        TODO()
    }
    private fun parseExpression(): ExpressionNode? {
        return parseLeftHandSideExpression() // temp
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

private inline fun Token.isKeyword(keyword: Keyword) =
    type == IDENTIFIER && rawContent == keyword.value
