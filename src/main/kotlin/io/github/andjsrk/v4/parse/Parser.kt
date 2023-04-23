package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.WasSuccessful
import io.github.andjsrk.v4.error.Error
import io.github.andjsrk.v4.error.SyntaxError
import io.github.andjsrk.v4.parse.Keyword.*
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.node.BinaryOperationNode
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
    private fun <T> elvisIfNoError(vararg values: Lazy<T?>) =
        values.fold(null as T?) { acc, it ->
            if (hasError) acc
            else acc ?: it.value
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
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-Identifier
    @Careful
    private fun parseIdentifier(): IdentifierNode? {
        if (currToken.type != IDENTIFIER) return null
        if (Keyword.values().any { it.value == currToken.rawContent }) return null

        return IdentifierNode(currToken).alsoAdvance()
    }
    // https://tc39.es/ecma262/multipage/ecmascript-language-lexical-grammar.html#prod-IdentifierName
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
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ArrayLiteral
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
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ComputedPropertyName
    private fun parseComputedPropertyName(property: PropertyNode.Unsealed): ExpressionNode? {
        property.startRange = takeIfMatches(LEFT_BRACKET)?.range ?: return null
        val expression = parseExpression() ?: return null
        expect(RIGHT_BRACKET) ?: return null

        property.isComputed = true

        return expression
    }
    // changed; https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-LiteralPropertyName
    @Careful
    private fun parseLiteralPropertyName() =
        parseIdentifierName() ?: parseStringLiteral()
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-PropertyName
    private fun parsePropertyName(property: PropertyNode.Unsealed) =
        parseLiteralPropertyName() ?: parseComputedPropertyName(property)
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
                if (currToken.type == COLON) { // { a: b }
                    // we must check whether the key is valid first
                    if (propertyName == null) return reportUnexpectedToken()

                    property.key = propertyName
                    advance() // skip colon
                    property.value = parseExpression() ?: return null // temp
                    property.startRange = property.key.range
                    property.toSealed()
                } else {
                    if (identifier == null) TODO("Method definition is not supported yet")
                    PropertyShorthandNode(identifier)
                }
            }
            else -> reportUnexpectedToken()
        }
    }
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ObjectLiteral
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
        takeIfMatchesKeyword(THIS)?.let(::ThisReferenceNode)
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
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-Arguments
    private fun parseArguments(call: FixedCalleeCallExpressionNode.Unsealed): List<CommaSeparatedElementNode>? {
        val args = mutableListOf<CommaSeparatedElementNode>()

        expect(LEFT_PAREN) ?: return null
        var skippedComma = true
        while (currToken.type != RIGHT_PAREN) {
            if (!skippedComma || currToken.type == COMMA) return reportUnexpectedToken()
            args += parseArgumentItem() ?: return null
            skippedComma = skip(COMMA)
        }
        call.endRange = expect(RIGHT_PAREN)?.range ?: return null

        return args.toList()
    }
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-MemberExpression
    private fun parseMemberExpression(`object`: ExpressionNode?, new: NewExpressionNode.Unsealed? = null): ExpressionNode? {
        val parsingNewExpression = new != null
        if (`object` == null) { // base case
            val primary = parsePrimaryExpression() ?: run {
                if (!hasError) reportUnexpectedToken()
                return null
            }
            return parseMemberExpression(primary, new)
        }

        val member = MemberExpressionNode.Unsealed()
        member.`object` = `object`
        val questionPeriod = takeIfMatches(QUESTION_DOT)
        member.isOptionalChain = questionPeriod != null

        if (member.isOptionalChain && parsingNewExpression) return reportUnexpectedToken(questionPeriod!!)

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
                if (member.isOptionalChain.not()) {
                    if (currToken.afterLineTerminator.not()) return reportUnexpectedToken()
                    return `object`
                }
                member.property = parseIdentifierName()!!
                member.endRange = member.property.range
            }
            else -> {
                if (parsingNewExpression && currToken.type == LEFT_PAREN) {
                    requireNotNull(new)
                    new.callee = `object`
                    new.arguments += parseArguments(new) ?: return null
                    return new.toSealed()
                }

                return `object`
            }
        }
        return parseMemberExpression(member.toSealed(), new)
    }
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-NewExpression
    private fun parseNewExpression(new: NewExpressionNode.Unsealed? = null): ExpressionNode? {
        val isCurrTokenNew = currToken.isKeyword(NEW)

        return (
            if (!isCurrTokenNew) parseMemberExpression(null, new)
            else {
                val freshNew = NewExpressionNode.Unsealed()
                freshNew.startRange = advance().range
                return parseNewExpression(freshNew)
            }
        )
    }
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-SuperCall
    private fun parseSuperCall(): SuperCallNode? {
        val superCall = SuperCallNode.Unsealed()

        val superToken = takeIfMatchesKeyword(SUPER) ?: return null
        superCall.startRange = superToken.range
        takeIfMatches(LEFT_PAREN) ?: return reportErrorMessage(SyntaxError.SUPER_NOT_CALLED, superToken.range)
        superCall.arguments += parseArguments(superCall) ?: return null
        expect(RIGHT_PAREN) ?: return null

        return superCall.toSealed()
    }
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ImportCall
    private fun parseImportCall(): ImportCallNode? {
        val importCall = ImportCallNode.Unsealed()

        val importToken = takeIfMatchesKeyword(IMPORT) ?: return null
        importCall.startRange = importToken.range
        importCall.pathSpecifier = parseExpression() ?: return null

        return importCall.toSealed()
    }
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-CallExpression
    private fun parseCallExpression(): CallSyntaxExpressionNode? {
        return when {
            currToken.isKeyword(SUPER) -> parseSuperCall()
            currToken.isKeyword(IMPORT) -> parseImportCall()
            else -> {
                val call = CallExpressionNode.Unsealed()
                call.callee = parseMemberExpression(null) ?: return null
                call.startRange = call.callee.range
                call.arguments += parseArguments(call) ?: return null
                call.toSealed()
            }
        }
    }
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-LeftHandSideExpression
    private fun parseLeftHandSideExpression() =
        elvisIfNoError(
            lazy { parseNewExpression() },
            lazy { parseCallExpression() },
        )
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-UnaryExpression
    private fun parseUnaryExpression() {
        TODO()
    }
    // https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-RelationalExpression
    private fun parseRelationalExpression(): BinaryOperationNode? {
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

        expect(LEFT_PAREN) ?: return null
        `if`.test = parseExpression() ?: return null
        expect(RIGHT_PAREN) ?: return null
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

        while (currToken.type != EOS) {
            statements += parseModuleItem() ?: return null
        }

        return ProgramNode(statements)
    }
}

private inline val IdentifierNode.canBeBooleanLiteral get() =
    value.toBooleanStrictOrNull() != null
private inline fun Token.isKeyword(keyword: Keyword) =
    type == IDENTIFIER && rawContent == keyword.value
