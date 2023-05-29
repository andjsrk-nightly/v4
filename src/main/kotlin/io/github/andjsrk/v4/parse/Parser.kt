package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.BinaryOperationType as BinaryOp
import io.github.andjsrk.v4.error.ErrorKind
import io.github.andjsrk.v4.error.SyntaxErrorKind
import io.github.andjsrk.v4.parse.ReservedWord.*
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.tokenize.Token
import io.github.andjsrk.v4.tokenize.TokenType
import io.github.andjsrk.v4.tokenize.TokenType.*
import io.github.andjsrk.v4.tokenize.Tokenizer

private val testing = System.getenv("TEST")?.toBooleanStrict() ?: false

class Parser(sourceText: String) {
    internal inner class CheckPoint {
        private val currToken = this@Parser.currToken
        private val tokenizerCheckPoint = tokenizer.CheckPoint()
        fun load() {
            this@Parser.currToken = currToken
            tokenizerCheckPoint.load()
        }
    }
    private val tokenizer = Tokenizer(sourceText)
    private var currToken = tokenizer.getNextToken()
    var error: Error? = null
        private set
    val hasError get() =
        error != null
    private val parseCtxs = Stack(
        ParseContext(allowModuleItem=true),
    )
    private inline fun <R> withParseContext(ctxProvider: ParseContext.() -> ParseContext, block: () -> R): R {
        val ctx = ctxProvider(parseCtxs.top)
        parseCtxs.push(ctx)
        return try { block() } finally {
            val popped = parseCtxs.pop()
            assert(popped === ctx)
        }
    }
    /**
     * Represents stack trace about where the last call of [reportError] is.
     * This can be used for debugging.
     */
    var stackTrace: List<StackTraceElement>? = null
        private set
    private var isLastStatementTerminated = true
    private fun advance(): Token {
        val prev = currToken
        currToken = tokenizer.getNextToken()
        return prev
    }
    private fun skip(tokenType: TokenType): WasSuccessful =
        (currToken.type == tokenType).thenAlso {
            advance()
        }
    private fun takeIfMatches(tokenType: TokenType) =
        (currToken.type == tokenType).thenTake { advance() }
    private fun takeIfMatchesKeyword(keyword: ReservedWord) =
        currToken.isKeyword(keyword).thenTake { advance() }
    private inline fun expect(tokenType: TokenType, check: (Token) -> Boolean = { true }) =
        if (currToken.type == tokenType && check(currToken)) advance()
        else reportUnexpectedToken()
    private fun expectKeyword(keyword: ReservedWord) =
        expect(IDENTIFIER) { it.isKeyword(keyword) }
    private fun reportError(error: Error): Nothing? {
        if (!hasError) {
            this.error = error
            if (testing) {
                try {
                    throw KotlinError()
                } catch (e: KotlinError) {
                    stackTrace = e.stackTrace
                        .drop(1) // drop first element because it is always this function
                        .takeWhile { it.not { isNativeMethod } } // preserve meaningful elements only
                }
            }
        }
        return null
    }
    private fun reportError(kind: ErrorKind, range: Range = currToken.range, vararg args: String) =
        reportError(Error(kind, range, args.isNotEmpty().thenTake { args.asList() }))
    private fun reportUnexpectedToken(token: Token = currToken): Nothing? {
        val kind = when (token.type) {
            EOS -> SyntaxErrorKind.UNEXPECTED_EOS
            NUMBER, BIGINT -> SyntaxErrorKind.UNEXPECTED_TOKEN_NUMBER
            STRING -> SyntaxErrorKind.UNEXPECTED_TOKEN_STRING
            IDENTIFIER -> SyntaxErrorKind.UNEXPECTED_TOKEN_IDENTIFIER
            in TEMPLATE_FULL..TEMPLATE_TAIL -> SyntaxErrorKind.UNEXPECTED_TEMPLATE_STRING
            ILLEGAL -> SyntaxErrorKind.INVALID_OR_UNEXPECTED_TOKEN
            else -> return reportError(
                SyntaxErrorKind.UNEXPECTED_TOKEN,
                token.range,
                token.type.staticContent ?: token.rawContent.ifEmpty { token.type.name },
            )
        }
        return reportError(kind, token.range)
    }
    private inline fun reportDuplicateName(
        names: List<IdentifierNode>,
        reporter: (IdentifierNode) -> Unit = {
            reportError(SyntaxErrorKind.VAR_REDECLARATION, it.range, it.value)
        },
    ) {
        val duplicate = names.findDuplicateBoundName(names.toRawNames())
        if (duplicate != null) reporter(duplicate)
    }
    private inline fun <R> ifHasNoError(valueProvider: () -> R) =
        not { hasError }.thenTake(valueProvider)
    private fun <T> T?.pipeIfHasNoError(valueProvider: () -> T?) =
        if (hasError) null
        else this ?: valueProvider()
    private fun <T> Iterable<Deferred<T>>.foldElvisIfHasNoError() =
        this.asSequence().foldNull<_, T> { acc, it ->
            acc ?: ifHasNoError(it)
        }
    private fun takeOptionalSemicolonRange(isEndOfStatement: Boolean = true) =
        takeIfMatches(SEMICOLON)?.range?.also {
            if (isEndOfStatement) isLastStatementTerminated = true
        }
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

        return currToken.toIdentifier().also { advance() }
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
                ?.also { advance() }
            else -> null
        }
    @ReportsErrorDirectly
    private fun parseArrayElement(): MaybeSpreadNode? {
        val ellipsisToken = takeIfMatches(ELLIPSIS)
        val expr = parseAssignment() ?: return null

        return (
            if (ellipsisToken != null) SpreadNode(expr, ellipsisToken.range)
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
    @Careful(false)
    private fun parseMethod(name: ObjectLiteralKeyNode, isAsync: Boolean, isGenerator: Boolean, startRange: Range): ObjectMethodNode? {
        val parameters = parseArrowFormalParameters() ?: return null
        val body = withParseContext({ copy(allowReturn=true) }) {
            parseBlock() ?: return null
        }
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
    private fun parseMethodLike(givenName: ObjectLiteralKeyNode? = null): ObjectElementNode? {
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
                            if (params.elements.isNotEmpty()) return reportError(SyntaxErrorKind.BAD_GETTER_ARITY, params.range)
                            ObjectGetterNode(actualName, method.body, name.range)
                        }
                        isSetter -> {
                            val param = params.elements.singleOrNull() ?: return reportError(SyntaxErrorKind.BAD_SETTER_ARITY, params.range)
                            if (param !is NonRestNode) return reportError(SyntaxErrorKind.BAD_SETTER_REST_PARAMETER, params.range)
                            ObjectSetterNode(actualName, param, method.body, name.range)
                        }
                        else -> neverHappens()
                    }
                }
            }
            else parseMethod(name, false, false, name.range)
        )
    }
    /**
     * Parses [PropertyDefinition](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-PropertyDefinition).
     */
    @ReportsErrorDirectly
    private fun parsePropertyDefinition(): ObjectElementNode? {
        if (currToken.type == ELLIPSIS) {
            val spreadToken = advance()
            val expression = parseAssignment() ?: return null
            return SpreadNode(expression, spreadToken.range)
        }

        val identifier = parseIdentifier()
        val propertyName = identifier ?: parsePropertyName() ?: return reportUnexpectedToken()
        return when (currToken.type) {
            COLON -> { // { a: b }
                advance()
                val value = parseAssignment() ?: return null
                PropertyNode(propertyName, value)
            }
            ASSIGN -> { // CoverInitializedName
                if (identifier == null) return reportUnexpectedToken()
                advance()
                val default = parseAssignment() ?: return null
                reportError(
                    SyntaxErrorKind.INVALID_COVER_INITIALIZED_NAME,
                    identifier.range..default.range,
                )
            }
            else ->
                listOf(
                    { parseMethodLike(propertyName) },
                    { PropertyShorthandNode(propertyName) },
                )
                    .foldElvisIfHasNoError()
        }
    }
    /**
     * Parses [ObjectLiteral](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-ObjectLiteral).
     */
    @ReportsErrorDirectly
    private fun parseObjectLiteral(): ObjectLiteralNode? {
        val elements = mutableListOf<ObjectElementNode>()

        val leftBraceTokenRange = takeIfMatches(LEFT_BRACE)?.range ?: return null
        var skippedComma = true
        while (currToken.type != RIGHT_BRACE) {
            if (!skippedComma) return reportUnexpectedToken()
            val element = parsePropertyDefinition() ?: return null
            elements += element
            skippedComma = skip(COMMA)
        }
        val rightBraceTokenRange = expect(RIGHT_BRACE)?.range ?: return null

        return ObjectLiteralNode(elements.toList(), leftBraceTokenRange..rightBraceTokenRange)
            .withEarlyErrorChecks()
    }
    private fun ObjectLiteralNode.withEarlyErrorChecks() =
        takeIf {
            val directSuperCall = elements
                .mapAsSequence {
                    (it as? FixedParametersMethodNode)
                        ?.findDirectSuperCall()
                }
                .foldElvis()
            when (directSuperCall) {
                null -> true
                else -> {
                    reportError(SyntaxErrorKind.UNEXPECTED_SUPER, directSuperCall.range)
                    false
                }
            }
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
            ?: allowBindingPattern.thenTake { parseBindingPattern() }
            ?: return reportError(SyntaxErrorKind.INVALID_REST_BINDING_PATTERN)
        when (currToken.type) {
            COMMA -> return reportError(SyntaxErrorKind.ELEMENT_AFTER_REST)
            ASSIGN -> return reportError(SyntaxErrorKind.REST_DEFAULT_INITIALIZER)
            else -> {}
        }
        return RestNode(binding, restTokenRange..binding.range)
    }
    @ReportsErrorDirectly
    private fun parseBindingElementWithoutInitializer(): BindingElementNode? =
        parseBindingIdentifier() ?: parseBindingPattern() ?: reportError(SyntaxErrorKind.INVALID_DESTRUCTURING_TARGET)
    /**
     * Parses [BindingElement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-BindingElement).
     */
    @ReportsErrorDirectly
    private fun parseBindingElement(): NonRestNode? {
        val binding = parseBindingElementWithoutInitializer() ?: return null
        takeIfMatches(ASSIGN) ?: return NonRestNode(binding, null)
        val default = parseAssignment() ?: return null
        return NonRestNode(binding, default)
            .withEarlyErrorChecks()
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
                if (left !is BindingElementNode) return reportUnexpectedToken()
                advance()
                val default = parseAssignment() ?: return null
                NonRestObjectPropertyNode(left, left, default)
            }
            else -> {
                if (left !is BindingElementNode) return reportUnexpectedToken()
                NonRestObjectPropertyNode(left, left, null)
            }
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
    private fun parseCoverParenthesizedExpressionAndArrowParameterList(): ExpressionNode? {
        if (currToken.type != LEFT_PARENTHESIS) return null

        val checkPoint = CheckPoint()
        // does not store current error because we are sure of there is no error

        val leftParenTokenRange = expect(LEFT_PARENTHESIS)?.range ?: neverHappens()
        val expr = parseExpression()
        val rightParenTokenAfterExprRange = expect(RIGHT_PARENTHESIS)?.range
        val exprError = error
        error = null

        checkPoint.load()

        val params = parseArrowFormalParameters(false)
        val paramsError = error
        error = null

        return (
            // because parameters will be parsed without care about errors, we need to check it on here
            if (currToken.type == ARROW) if (paramsError == null) params!! else reportError(paramsError)
            else expr?.let {
                ParenthesizedExpressionNode(it, leftParenTokenRange..rightParenTokenAfterExprRange!!)
            }
                ?: reportError(exprError!!)
        )
    }
    private fun parseMethodExpression(isAsync: Boolean, isGenerator: Boolean, startRange: Range?): MethodExpressionNode? {
        val methodTokenRange = takeIfMatchesKeyword(METHOD)?.range ?: return null
        val params = parseArrowFormalParameters() ?: return null
        val body = withParseContext({ copy(allowReturn=true, allowAwait=isAsync) }) {
            parseBlock() ?: return null
        }
        return MethodExpressionNode(params, body, isAsync, isGenerator, startRange ?: methodTokenRange)
    }
    @Careful(false)
    private fun parseClassElement(): ClassElementNode? {
        if (currToken.type == SEMICOLON) return EmptyStatementNode(advance().range)

        val staticToken = takeIfMatchesKeyword(STATIC)
        val isStatic = staticToken != null
        val name = parsePropertyName() ?: return null
        val startRange = staticToken?.range ?: name.range
        val value = parseInitializer()?.value

        return when {
            value != null -> // field with initializer
                FieldNode(name, value, isStatic, startRange, takeOptionalSemicolonRange(false))
            else -> parseMethodLike(name).run {
                when (this) {
                    // transform object elements to class elements
                    is ObjectGetterNode -> ClassGetterNode(name, body, isStatic, startRange)
                    is ObjectSetterNode -> ClassSetterNode(name, parameter, body, isStatic, startRange)
                    is ObjectMethodNode -> {
                        val actualName = this.name
                        if (actualName is IdentifierNode && actualName.isKeyword(CONSTRUCTOR)) {
                            if (isAsync) return reportError(SyntaxErrorKind.CONSTRUCTOR_IS_ASYNC, actualName.range)
                            if (isGenerator) return reportError(SyntaxErrorKind.CONSTRUCTOR_IS_GENERATOR, actualName.range)
                            return ConstructorNode(actualName, parameters, body)
                        }
                        ClassMethodNode(actualName, parameters, body, isAsync, isGenerator, isStatic, startRange)
                    }
                    null -> // field without initializer
                        ifHasNoError {
                            FieldNode(name, null, isStatic, startRange, takeOptionalSemicolonRange(false))
                        }
                    else -> neverHappens()
                }
            }
        }
    }
    @ReportsErrorDirectly
    private fun parseClassTail(): ClassTailNode? {
        val extendsTokenRange = takeIfMatchesKeyword(EXTENDS)?.range
        val parent =
            if (extendsTokenRange != null) parseLeftHandSideExpression()
            else null
        val leftBraceTokenRange = expect(LEFT_BRACE)?.range ?: return null
        val elements = mutableListOf<ClassElementNode>()
        var lastElement: ClassElementNode? = null
        while (currToken.type != RIGHT_BRACE) {
            if (lastElement is FieldNode) {
                val hasNoSemicolon = lastElement.range.end == (lastElement.value ?: lastElement.name).range.end
                if (hasNoSemicolon && currToken.not { isPrevLineTerminator }) return reportUnexpectedToken()
            }
            lastElement = parseClassElement() ?: return null
            elements += lastElement
        }
        val endRange = expect(RIGHT_BRACE)?.range ?: neverHappens()
        return ClassTailNode(parent, elements.toList(), (extendsTokenRange ?: leftBraceTokenRange)..endRange)
    }
    @ReportsErrorDirectly
    private fun parseClassExpression(): ClassExpressionNode? {
        val startRange = takeIfMatchesKeyword(CLASS)?.range ?: return null
        val name = parseIdentifier()
        val tail = parseClassTail() ?: return null
        return ClassExpressionNode(name, tail.parent, tail.elements, startRange..tail.range)
            .withEarlyErrorChecks()
    }
    @ReportsErrorDirectly
    private fun <N: ClassNode> N.withEarlyErrorChecks() =
        takeIf {
            val constructor = constructor
            when {
                parent == null && constructor != null ->
                    when (val directSuperCall = constructor.findDirectSuperCall()) {
                        null -> true
                        else -> {
                            reportError(SyntaxErrorKind.UNEXPECTED_SUPER, directSuperCall.range)
                            false
                        }
                    }
                else -> true
            }
        }
    private fun parseTemplateLiteral(): TemplateLiteralNode? {
        val head = takeIfMatches(TEMPLATE_FULL) ?: takeIfMatches(TEMPLATE_HEAD) ?: return null
        if (head.type == TEMPLATE_FULL) return TemplateLiteralNode(listOf(TemplateStringNode(head)), emptyList())
        val strings = mutableListOf(head)
        val expressions = mutableListOf<ExpressionNode>()
        while (true) {
            val expr = parseExpression() ?: return null
            tokenizer.back() // current token is right brace, which is not expected, so get back to previous state then get template middle token
            val string = tokenizer.getTemplateMiddleToken()
            if (string.type.not { isOneOf(TEMPLATE_MIDDLE, TEMPLATE_TAIL) }) return reportError(SyntaxErrorKind.UNTERMINATED_TEMPLATE_EXPR)
            advance() // successfully got template middle/tail token, so now advance to get normal token
            expressions += expr
            strings += string
            if (string.type == TEMPLATE_TAIL) break
        }
        return TemplateLiteralNode(strings.map(::TemplateStringNode), expressions.toList())
    }
    /**
     * Parses [PrimaryExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-PrimaryExpression).
     */
    @Careful(false)
    private fun parsePrimaryExpression(): ExpressionNode? =
        parsePrimitiveLiteral() ?: parseIdentifier() ?: when (currToken.type) {
            IDENTIFIER -> // now there are only keywords except primitive literals
                listOf(
                    ::parseThis,
                    ::parseClassExpression,
                )
                    .foldElvisIfHasNoError()
            LEFT_PARENTHESIS -> parseCoverParenthesizedExpressionAndArrowParameterList()
            LEFT_BRACE -> parseObjectLiteral()
            LEFT_BRACKET -> parseArrayLiteral()
            TEMPLATE_FULL, TEMPLATE_HEAD -> parseTemplateLiteral()
            else -> null
        }
    // </editor-fold>
    @Careful(false)
    private fun parseArgument(): MaybeSpreadNode? {
        if (currToken.type == ELLIPSIS) {
            val spreadTokenRange = advance().range
            val expr = parseAssignment() ?: return null
            return SpreadNode(expr, spreadTokenRange)
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
        val parsingSuperProperty = `object` == null && currToken.isKeyword(SUPER)
        val `super` = parsingSuperProperty.thenTake {
            SuperNode(advance().range)
        }
        if (parsingNew && parsingSuperProperty) return reportError(SyntaxErrorKind.UNEXPECTED_SUPER, `super`!!.range)

        if (`object` == null && !parsingSuperProperty) { // base case
            val primary = parsePrimaryExpression() ?: return null
            if (primary is UniqueFormalParametersNode) return primary
            return parseMemberExpression(primary, parsingNew)
        }

        val actualObject = `object` ?: `super` ?: neverHappens()
        val questionDotToken = takeIfMatches(QUESTION_DOT)
        val isOptionalChain = questionDotToken != null

        if (parsingNew && isOptionalChain) return reportError(SyntaxErrorKind.NEW_OPTIONAL_CHAIN, questionDotToken!!.range)
        if (parsingSuperProperty && isOptionalChain) return reportError(SyntaxErrorKind.SUPER_OPTIONAL_CHAIN, questionDotToken!!.range)

        lateinit var property: ExpressionNode
        lateinit var endRange: Range
        var isComputed = false
        when (currToken.type) {
            LEFT_BRACKET -> {
                advance()
                property = parseExpression() ?: return reportUnexpectedToken()
                endRange = expect(RIGHT_BRACKET)?.range ?: return null
                isComputed = true
            }
            DOT -> {
                if (isOptionalChain) return reportUnexpectedToken()
                advance()
                property = parseIdentifierName() ?: return reportUnexpectedToken()
                endRange = property.range
            }
            IDENTIFIER -> {
                if (!isOptionalChain) {
                    if (parsingSuperProperty) return reportError(SyntaxErrorKind.UNEXPECTED_SUPER, `super`!!.range)
                    return `object`
                }
                property = parseIdentifierName()!!
                endRange = property.range
            }
            else -> {
                if (!parsingNew) {
                    val isNormalCall = currToken.type == LEFT_PARENTHESIS
                    val isTaggedTemplate = !parsingSuperProperty && currToken.type.isTemplateStart && currToken.not { isPrevLineTerminator }
                    if (isNormalCall || isTaggedTemplate) return parseCall(actualObject, isOptionalChain)
                }

                // if it is neither member access nor call, it is an unexpected token
                if (isOptionalChain) return reportUnexpectedToken()
                if (parsingSuperProperty) return reportError(SyntaxErrorKind.UNEXPECTED_SUPER, `super`!!.range)

                return `object`
            }
        }

        return parseMemberExpression(
            when (actualObject) {
                is SuperNode -> SuperPropertyNode(`super`!!, property, isComputed, endRange)
                else -> MemberExpressionNode(
                    actualObject,
                    property,
                    isOptionalChain,
                    isComputed,
                    endRange,
                )
            },
            parsingNew,
        )
    }
    /**
     * Parses [SuperCall](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-SuperCall).
     */
    @ReportsErrorDirectly
    private fun parseSuperCall(): SuperCallNode? {
        val superNode = takeIfMatchesKeyword(SUPER)
            ?.let { SuperNode(it.range) }
            ?: return null
        if (currToken.type != LEFT_PARENTHESIS) return reportError(SyntaxErrorKind.UNEXPECTED_SUPER, superNode.range)
        val args = parseArguments() ?: return null

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
        val pathSpecifier = parseAssignment() ?: return null
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
            else when {
                currToken.type == LEFT_PARENTHESIS -> {
                    val args = parseArguments() ?: return null
                    when (callee) {
                        is SuperNode -> SuperCallNode(callee, args)
                        else -> NormalCallNode(callee, args, isOptionalChain)
                    }
                }
                currToken.type.isTemplateStart -> {
                    if (isOptionalChain) return reportError(SyntaxErrorKind.TAGGED_TEMPLATE_OPTIONAL_CHAIN)
                    val template = parseTemplateLiteral() ?: return null
                    TaggedTemplateNode(callee, template)
                }
                else ->
                    callee.takeUnless { isOptionalChain }
                        ?: reportUnexpectedToken()
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
    /**
     * Parses [LeftHandSideExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-LeftHandSideExpression).
     */
    @Careful(false)
    private fun parseLeftHandSideExpression() =
        listOf(
            ::parseNewExpression,
            { parseCall(null) },
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
                    .withEarlyErrorChecks()
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
                        .withEarlyErrorChecks()
                } else leftHandSideExpr
            }
        }
    }
    private fun UpdateNode.withEarlyErrorChecks() =
        takeIf {
            when {
                operand.isValidAssignmentTarget() -> true
                else -> {
                    reportError(
                        if (isPrefixed) SyntaxErrorKind.INVALID_LHS_IN_PREFIX_OP
                        else SyntaxErrorKind.INVALID_LHS_IN_POSTFIX_OP,
                        operand.range,
                    )
                    false
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
        if (expr is UnaryExpressionNode && expr !is UpdateNode) return reportError(
            SyntaxErrorKind.UNEXPECTED_TOKEN_UNARY_EXPONENTIATION,
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
    private tailrec fun parseRelational(left: BinaryExpressionNode? = null): ExpressionNode? {
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
        return parseRelational(BinaryExpressionNode(shiftExpr, right, operation))
    }
    /**
     * Parses [EqualityExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-EqualityExpression).
     */
    @Careful(false)
    private tailrec fun parseEquality(left: BinaryExpressionNode? = null): ExpressionNode? {
        val relationalExpr = left ?: parseRelational() ?: return null
        if (currToken.type !in EQ..NOT_EQ_STRICT) return relationalExpr
        val operation = BinaryOp.fromTokenType(currToken.type)
        advance()
        val right = parseRelational() ?: return null
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
     * Parses IfExpression.
     *
     * IfExpression `::`
     *   `if` `(` Expression `)` AssignmentExpression `else` AssignmentExpression
     */
    private fun parseIfExpression(): ExpressionNode? {
        val startRange = takeIfMatchesKeyword(IF)?.range ?: return parseShortCircuit()
        expect(LEFT_PARENTHESIS) ?: return null
        val test = parseExpression() ?: return null
        expect(RIGHT_PARENTHESIS) ?: return null
        val then = parseAssignment() ?: return null
        takeIfMatchesKeyword(ELSE) ?: return reportUnexpectedToken()
        val `else` = parseAssignment() ?: return null
        return IfExpressionNode(test, then, `else`, startRange)
    }
    /**
     * Parses [YieldExpression](https://tc39.es/ecma262/multipage/ecmascript-language-functions-and-classes.html#prod-YieldExpression).
     */
    @Careful(false)
    private fun parseYield(): YieldNode? {
        val yieldTokenRange = takeIfMatchesKeyword(YIELD)?.range ?: return null
        if (currToken.isPrevLineTerminator) return reportError(SyntaxErrorKind.NEWLINE_AFTER_YIELD)
        val isSpread = takeIfMatches(ELLIPSIS) != null
        val expr = parseAssignment() ?: return null
        return YieldNode(expr, isSpread, yieldTokenRange)
    }
    // <editor-fold desc="arrow function">
    /**
     * Parses [ConciseBody](https://tc39.es/ecma262/multipage/ecmascript-language-functions-and-classes.html#prod-ConciseBody).
     */
    @Careful(false)
    private fun parseConciseBody() =
        if (currToken.type == LEFT_BRACE)
            withParseContext({ copy(allowReturn=true) }) {
                parseBlock()
            }
        else parseAssignment()
    @ReportsErrorDirectly
    private fun parseArrowFunctionWithoutParenthesis(parameter: IdentifierNode): ArrowFunctionNode? {
        // current token is =>

        if (!currToken.not { isPrevLineTerminator }) return reportUnexpectedToken()
        advance()

        val body = parseConciseBody() ?: return null

        return ArrowFunctionNode(
            UniqueFormalParametersNode(
                listOf(NonRestNode(parameter, null)),
                parameter.range,
            ),
            body,
            false,
            false,
            parameter.range..body.range,
        )
    }
    /**
     * @see UniqueFormalParametersNode.withEarlyErrorChecks
     */
    @ReportsErrorDirectly
    private fun <N: NonRestNode> N.withEarlyErrorChecks() =
        when {
            default is YieldNode ->
                reportError(SyntaxErrorKind.YIELD_IN_PARAMETER, range)
            default is UnaryExpressionNode && default.operation == UnaryOperationType.AWAIT ->
                reportError(SyntaxErrorKind.AWAIT_EXPRESSION_FORMAL_PARAMETER, range)
            else -> this
        }
    /**
     * Parses [ArrowFormalParameters](https://tc39.es/ecma262/multipage/ecmascript-language-functions-and-classes.html#prod-ArrowFormalParameters).
     */
    @ReportsErrorDirectly
    private fun parseArrowFormalParameters(shouldBreakIfInvalid: Boolean = true): UniqueFormalParametersNode? {
        val startRange = expect(LEFT_PARENTHESIS)?.range ?: return null
        val elements = mutableListOf<MaybeRestNode>()
        var skippedComma = true
        var encounteredInvalid = false

        fun encounterInvalidOrNull() =
            not { shouldBreakIfInvalid }.thenTake {
                encounteredInvalid = true
            }

        while (currToken.type != RIGHT_PARENTHESIS) {
            if (!shouldBreakIfInvalid && encounteredInvalid) {
                // will advance until encounter right parenthesis to determine branch of CoverParenthesizedExpressionAndArrowParameterList
                advance()
                continue
            }

            if (!skippedComma) {
                reportUnexpectedToken()
                encounterInvalidOrNull() ?: return null
            }

            if (currToken.type == ELLIPSIS) {
                val restElement = parseBindingRestElement()
                if (restElement != null) {
                    elements += restElement
                    if (shouldBreakIfInvalid) break
                } else encounterInvalidOrNull() ?: return null
            } else {
                val element = parseBindingElement()
                if (element != null) elements += element
                else encounterInvalidOrNull() ?: return null
            }
            skippedComma = skip(COMMA)
        }
        val endRange = expect(RIGHT_PARENTHESIS)?.range ?: return null

        return UniqueFormalParametersNode(elements, startRange..endRange)
            .withEarlyErrorChecks()
    }
    /**
     * See [Early Errors](https://tc39.es/ecma262/multipage/ecmascript-language-functions-and-classes.html#sec-arrow-function-definitions-static-semantics-early-errors).
     * Note that this early error rule will be applied to methods as well because `await`/`yield` expressions are not contextually allowed anymore.
     * Also, `await`/`yield` expressions will be handled on [NonRestNode.withEarlyErrorChecks],
     * because finding range of those in this function is less efficient.
     */
    @ReportsErrorDirectly
    private fun UniqueFormalParametersNode.withEarlyErrorChecks() =
        takeIf {
            reportDuplicateName(boundNames()) {
                reportError(SyntaxErrorKind.DUPLICATE_PARAMETER_NAMES, it.range)
            }
            !hasError
        }
    @ReportsErrorDirectly
    private fun parseArrowFunctionByUniqueFormalParameters(params: UniqueFormalParametersNode): ArrowFunctionNode? {
        // current token is =>

        if (!currToken.not { isPrevLineTerminator }) return reportUnexpectedToken()
        advance()

        val body = parseConciseBody() ?: return null

        return ArrowFunctionNode(
            params,
            body,
            false,
            false,
            params.range..body.range,
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
            if (currToken.type == LEFT_PARENTHESIS) parseArrowFormalParameters() ?: return null
            else {
                val paramName = parseIdentifier() ?: return null
                UniqueFormalParametersNode(
                    listOf(NonRestNode(paramName, null)),
                    paramName.range,
                )
            }
        expect(ARROW) ?: return null
        val body = withParseContext({ copy(allowAwait=isAsync) }) {
            parseConciseBody() ?: return null
        }
        return ArrowFunctionNode(parameters, body, isAsync, isGenerator, nonNullStartRange..body.range)
    }
    private fun parseSpecialFunction(): SpecialFunctionExpressionNode? {
        val asyncToken = takeIfMatchesKeyword(ASYNC)
        val genToken = takeIfMatchesKeyword(GEN)
        return (
            when {
                currToken.isKeyword(METHOD) -> ::parseMethodExpression
                else -> ::parseArrowFunction
            }(
                asyncToken != null,
                genToken != null,
                (asyncToken ?: genToken)?.range,
            )
        )
    }
    // </editor-fold>
    /**
     * Parses [AssignmentExpression](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-AssignmentExpression).
     */
    @Careful(false)
    private fun parseAssignment(): ExpressionNode? =
        parseYield()
            .pipeIfHasNoError {
                parseIfExpression()
                    ?.let {
                        when (it) {
                            is IdentifierNode ->
                                if (currToken.type == ARROW) parseArrowFunctionWithoutParenthesis(it)
                                else it

                            is UniqueFormalParametersNode ->
                                // we are sure of current token is =>
                                parseArrowFunctionByUniqueFormalParameters(it)

                            else -> it
                        }
                    }
                    ?.let {
                        when {
                            it.isLeftHandSide -> {
                                if (currToken.type.not { isAssignLike }) return@let it
                                val operation = BinaryOp.fromTokenType(currToken.type)
                                advance()
                                val right = parseAssignment() ?: return@let null
                                BinaryExpressionNode(it, right, operation)
                            }

                            else -> it
                        }
                    }
                    ?: when {
                        currToken.isKeyword(ASYNC) || currToken.isKeyword(GEN) -> parseSpecialFunction()
                        currToken.isKeyword(METHOD) -> parseMethodExpression(false, false, null)
                        else -> reportUnexpectedToken()
                    }
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
            else SequenceExpressionNode(exprs.toList(), expr.range..lastExpr.range)
        )
    }
    // </editor-fold>
    /**
     * Parses [ExpressionStatement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-ExpressionStatement).
     */
    @Careful(false)
    private fun parseExpressionStatement(): ExpressionStatementNode? {
        val expr = parseExpression() ?: return null
        return ExpressionStatementNode(expr, takeOptionalSemicolonRange())
    }
    /**
     * Parses [BlockStatement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-BlockStatement).
     *
     * @param doDuplicateCheck If `false`, can be used for reducing cost by delegating responsibility about duplicate check to caller.
     */
    @ReportsErrorDirectly
    private fun parseBlock(doDuplicateCheck: Boolean = true): BlockNode? {
        val statements = mutableListOf<StatementNode>()

        val startRange = expect(LEFT_BRACE)?.range ?: return null
        while (currToken.type != RIGHT_BRACE) statements += parseStatementListItem() ?: return null
        val endRange = expect(RIGHT_BRACE)?.range ?: return null

        return BlockNode(statements, startRange..endRange)
            .let { block ->
                if (doDuplicateCheck) block.withEarlyErrorChecks()
                else block
            }
    }
    private fun <N: StatementListNode> N.withEarlyErrorChecks() =
        takeIf {
            reportDuplicateName(lexicallyDeclaredNames())
            !hasError
        }
    /**
     * Parses [IfStatement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-IfStatement).
     */
    @ReportsErrorDirectly
    private fun parseIfStatement(): IfStatementNode? {
        val startRange = takeIfMatchesKeyword(IF)?.range ?: return null

        expect(LEFT_PARENTHESIS) ?: return null
        val test = parseExpression() ?: return null
        expect(RIGHT_PARENTHESIS) ?: return null
        val then = parseStatement() ?: return null
        takeIfMatchesKeyword(ELSE) ?: return IfStatementNode(test, then, null, startRange)
        val `else` = parseStatement() ?: return null

        return IfStatementNode(test, then, `else`, startRange)
    }
    /**
     * Parses [ForStatement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-ForStatement) and [ForInOfStatement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-ForInOfStatement).
     */
    @ReportsErrorDirectly
    private fun parseFor(): ForNode? {
        fun parseForHeadElement(givenElement: Node? = null, after: TokenType = SEMICOLON): Node? {
            if (takeIfMatches(after) != null) return null

            val expr = givenElement ?: parseExpression() ?: return reportUnexpectedToken()
            expect(after) ?: return null
            return expr
        }

        val startRange = takeIfMatchesKeyword(FOR)?.range ?: return null
        expect(LEFT_PARENTHESIS) ?: return null
        val isInitOmittedNormalFor = currToken.type == SEMICOLON // do not take because ForDeclaration needs to be parsed on below
        val declaration = parseLexicalDeclarationWithoutInitializer()
        if (hasError) return null
        return when {
            declaration != null && currToken.isKeyword(IN) -> {
                advance()
                val target = parseAssignment() ?: return null
                expect(RIGHT_PARENTHESIS) ?: return null
                val body = parseStatement() ?: return null
                ForInNode(declaration, target, body, startRange)
            }
            else -> {
                val init = if (declaration == null) {
                    if (isInitOmittedNormalFor) null
                    else reportUnexpectedToken()
                } else parseLexicalDeclaration(declaration, false)
                expect(SEMICOLON)
                if (hasError) return null
                val test = parseForHeadElement() as ExpressionNode?
                if (hasError) return null
                val update = parseForHeadElement(after=RIGHT_PARENTHESIS) as ExpressionNode?
                if (hasError) return null
                // right parenthesis will be handled on `update`'s parsing
                val body = parseStatement() ?: return null
                NormalForNode(init, test, update, body, startRange)
            }
        }
    }
    /**
     * Parses [WhileStatement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-WhileStatement).
     */
    @ReportsErrorDirectly
    private fun parseWhile(): WhileNode? {
        val startRange = takeIfMatchesKeyword(WHILE)?.range ?: return null
        val atLeastOnce = takeIfMatches(PLUS) != null
        expect(LEFT_PARENTHESIS) ?: return null
        val test = parseExpression() ?: return null
        expect(RIGHT_PARENTHESIS) ?: return null
        val body = parseStatement() ?: return null
        return WhileNode(test, body, atLeastOnce, startRange)
    }
    /**
     * Parses [IterationStatement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-IterationStatement).
     */
    @Careful(false)
    private fun parseIterationStatement(): IterationStatementNode? {
        return withParseContext({ copy(allowIterationFlowControlStatement=true) }) {
            listOf(
                ::parseFor,
                ::parseWhile,
            )
                .foldElvisIfHasNoError()
        }
    }
    /**
     * Parses [ContinueStatement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-ContinueStatement).
     */
    @Careful
    private fun parseContinue(): ContinueNode? {
        val ctx = parseCtxs.top
        val continueTokenRange = takeIfMatchesKeyword(CONTINUE)?.range ?: return null
        if (ctx.not { allowIterationFlowControlStatement }) return reportError(SyntaxErrorKind.ILLEGAL_CONTINUE, continueTokenRange)
        return ContinueNode(continueTokenRange, takeOptionalSemicolonRange())
    }
    /**
     * Parses [BreakStatement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-BreakStatement).
     */
    @Careful
    private fun parseBreak(): BreakNode? {
        val ctx = parseCtxs.top
        val breakTokenRange = takeIfMatchesKeyword(BREAK)?.range ?: return null
        if (ctx.not { allowIterationFlowControlStatement }) return reportError(SyntaxErrorKind.ILLEGAL_BREAK, breakTokenRange)
        return BreakNode(breakTokenRange, takeOptionalSemicolonRange())
    }
    /**
     * Parses [ReturnStatement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-ReturnStatement).
     */
    @ReportsErrorDirectly
    private fun parseReturn(): ReturnNode? {
        val ctx = parseCtxs.top
        val returnTokenRange = takeIfMatchesKeyword(RETURN)?.range ?: return null
        if (ctx.not { allowReturn }) return reportError(SyntaxErrorKind.ILLEGAL_RETURN, returnTokenRange)
        val isCurrSemicolon = currToken.type == SEMICOLON
        if (isCurrSemicolon || currToken.type.isAsiJob || currToken.isPrevLineTerminator) return ReturnNode(
            null,
            returnTokenRange,
            isCurrSemicolon.thenTake { advance().range },
        )
        val expr = parseAssignment() ?: return null
        return ReturnNode(expr, returnTokenRange, takeOptionalSemicolonRange())
    }
    /**
     * Parses [ThrowStatement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-ThrowStatement).
     */
    @Careful(false)
    private fun parseThrow(): ThrowNode? {
        val throwTokenRange = takeIfMatchesKeyword(THROW)?.range ?: return null
        if (currToken.isPrevLineTerminator) return reportError(SyntaxErrorKind.NEWLINE_AFTER_THROW, throwTokenRange)
        val expr = parseAssignment() ?: return null
        return ThrowNode(expr, throwTokenRange, takeOptionalSemicolonRange())
    }
    /**
     * Parses [TryStatement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#sec-try-statement).
     */
    private fun parseTry(): TryNode? {
        val startRange = takeIfMatchesKeyword(TRY)?.range ?: return null
        val tryBody = parseBlock() ?: return null
        val catch = run {
            val startRange = takeIfMatchesKeyword(CATCH)?.range ?: return@run null
            val binding = run binding@ {
                takeIfMatches(LEFT_PARENTHESIS)?.range ?: return@binding null
                val binding = parseBindingElementWithoutInitializer() ?: return null
                expect(RIGHT_PARENTHESIS) ?: return null
                binding
            }
            val body = parseBlock() ?: return null
            val bodyNames = body.lexicallyDeclaredNames()
            reportDuplicateName((binding?.boundNames() ?: emptyList()) + bodyNames)

            ifHasNoError { CatchNode(binding, body, startRange) }
        }
        val finallyBody = run {
            takeIfMatchesKeyword(FINALLY) ?: return@run null
            parseBlock() ?: return null
        }
        if (catch == null && finallyBody == null) return reportError(SyntaxErrorKind.NO_CATCH_OR_FINALLY, startRange..tryBody.range)
        return TryNode(tryBody, catch, finallyBody, startRange)
    }
    /**
     * Parses [Statement](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-Statement).
     */
    @Careful(false)
    private fun parseStatement(): StatementNode? =
        when (currToken.type) {
            SEMICOLON -> EmptyStatementNode(advance().range)
            IDENTIFIER -> listOf(
                ::parseIfStatement,
                ::parseIterationStatement,
                ::parseContinue,
                ::parseBreak,
                ::parseReturn,
                ::parseThrow,
                ::parseTry,
                ::parseExpressionStatement,
            )
                .foldElvisIfHasNoError()
            LEFT_BRACE -> parseBlock()
            else -> parseExpressionStatement()
        }
    /**
     * Parses [Initializer](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-Initializer).
     */
    @Careful(false)
    private fun parseInitializer(): InitializerNode? {
        val startRange = takeIfMatches(ASSIGN)?.range ?: return null
        val value = parseAssignment() ?: return null
        return InitializerNode(value, startRange)
    }
    /**
     * Parses [LexicalDeclaration](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-LexicalDeclaration) without [Initializer](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-Initializer).
     * @see LexicalDeclarationWithoutInitializerNode
     */
    @ReportsErrorDirectly
    private fun parseLexicalDeclarationWithoutInitializer(): LexicalDeclarationWithoutInitializerNode? {
        val kindToken = takeIfMatchesKeyword(VAR) ?: takeIfMatchesKeyword(LET) ?: return null

        val kind = LexicalDeclarationKind.valueOf(kindToken.rawContent.uppercase())
        val startToken = currToken
        val binding = parseIdentifier() ?: parseBindingPattern() ?: return reportUnexpectedToken(startToken)

        return LexicalDeclarationWithoutInitializerNode(kind, binding, startToken.range)
            .withEarlyErrorChecks()
    }
    /**
     * Parses [LexicalDeclaration](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-LexicalDeclaration).
     * @param inNormalContext
     * Indicates whether the lexical declaration is in normal context;
     * There are some special context such as for statement.
     */
    @ReportsErrorDirectly
    private fun parseLexicalDeclaration(givenDeclaration: LexicalDeclarationWithoutInitializerNode? = null, inNormalContext: Boolean = true): LexicalDeclarationNode? {
        assert(givenDeclaration !is LexicalDeclarationNode)
        val withoutInitializer = givenDeclaration ?: parseLexicalDeclarationWithoutInitializer() ?: return null
        val kind = withoutInitializer.kind
        val binding = withoutInitializer.binding
        val value = parseInitializer()?.value
        if (value == null) {
            if (hasError) return null
            if (kind == LexicalDeclarationKind.LET || binding is BindingPatternNode) return reportError(
                SyntaxErrorKind.DECLARATION_MISSING_INITIALIZER,
                binding.range,
            )
        }

        return LexicalDeclarationNode(
            kind,
            binding,
            value,
            withoutInitializer.range,
            inNormalContext.thenTake {
                takeOptionalSemicolonRange()
            },
        )
    }
    /**
     * See [Early Errors](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#sec-let-and-const-declarations-static-semantics-early-errors).
     */
    @ReportsErrorDirectly
    private fun LexicalDeclarationWithoutInitializerNode.withEarlyErrorChecks() =
        takeIf {
            reportDuplicateName(boundNames())
            !hasError
        }
    /**
     * Parses [ClassDeclaration](https://tc39.es/ecma262/multipage/ecmascript-language-functions-and-classes.html#prod-ClassDeclaration).
     */
    @ReportsErrorDirectly
    private fun parseClassDeclaration(): ClassDeclarationNode? {
        val startRange = takeIfMatchesKeyword(CLASS)?.range ?: return null
        val name = parseIdentifier() ?: return reportError(SyntaxErrorKind.MISSING_CLASS_NAME)
        val tail = parseClassTail() ?: return null
        return ClassDeclarationNode(name, tail.parent, tail.elements, startRange..tail.range)
            .withEarlyErrorChecks()
    }
    /**
     * Parses [Declaration](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-Declaration).
     */
    @Careful(false)
    private fun parseDeclaration() =
        listOf(
            { parseLexicalDeclaration() }, // `(Any?(optional)) -> Any?` is not assignable to `() -> Any?`
            ::parseClassDeclaration,
        )
            .foldElvisIfHasNoError()
    /**
     * Parses [StatementListItem](https://tc39.es/ecma262/multipage/ecmascript-language-statements-and-declarations.html#prod-StatementListItem).
     */
    @Careful(false)
    private fun parseStatementListItem() =
        listOf(
            ::parseDeclaration,
            ::parseStatement,
        )
            .foldElvisIfHasNoError()
    @ReportsErrorDirectly
    private fun parseFromClause(): StringLiteralNode? {
        expectKeyword(FROM) ?: return null
        return parseStringLiteral() ?: return reportUnexpectedToken()
    }
    @ReportsErrorDirectly
    private fun parseImportSpecifier(): ImportOrExportSpecifierNode? {
        val name = parseIdentifierName() ?: return reportUnexpectedToken()
        takeIfMatchesKeyword(AS) ?: return when {
            name.not { isIdentifier() } -> reportError(SyntaxErrorKind.UNEXPECTED_RESERVED, name.range)
            else -> ImportOrExportSpecifierNode(name, name)
        }
        val alias = parseBindingIdentifier() ?: return reportUnexpectedToken()
        return ImportOrExportSpecifierNode(name, alias)
    }
    /**
     * Returning `null`(not a pair that contains `null`) means there is an error.
     */
    @Careful(false)
    private fun parseImportClause(): Pair<DefaultImportBindingNode?, NonDefaultImportBindingNode?>? {
        val name = parseBindingIdentifier() ?: return parseImportClause(null)
        val default = DefaultImportBindingNode(name)
        takeIfMatches(COMMA) ?: return default to null
        return parseImportClause(default)
    }
    @ReportsErrorDirectly
    private fun parseImportClause(default: DefaultImportBindingNode?): Pair<DefaultImportBindingNode?, NonDefaultImportBindingNode?>? {
        return default to when (currToken.type) {
            MULTIPLY -> { // NameSpaceImport
                val startRange = advance().range
                expectKeyword(AS) ?: return null
                val ns = parseBindingIdentifier() ?: return reportUnexpectedToken()
                NamespaceImportBindingNode(ns, startRange)
            }
            LEFT_BRACE -> { // NamedImports
                val startRange = advance().range
                val specifiers = mutableListOf<ImportOrExportSpecifierNode>()
                var skippedComma = true
                while (currToken.type != RIGHT_BRACE) {
                    if (!skippedComma) return reportUnexpectedToken()
                    val specifier = parseImportSpecifier() ?: return null
                    specifiers += specifier
                    skippedComma = skip(COMMA)
                }
                val endRange = expect(RIGHT_BRACE)?.range ?: neverHappens()
                NamedImportBindingNode(specifiers.toList(), startRange..endRange)
            }
            else -> reportUnexpectedToken()
        }
    }
    @ReportsErrorDirectly
    private fun parseImportDeclaration(): ImportDeclarationNode? {
        val startRange = takeIfMatchesKeyword(IMPORT)?.range ?: return null
        parseStringLiteral()?.let {
            return ImportDeclarationNode(null, null, it, startRange, takeOptionalSemicolonRange())
        }
        val (default, nonDefault) = parseImportClause() ?: return null
        val moduleSpecifier = parseFromClause() ?: return null
        return ImportDeclarationNode(default, nonDefault, moduleSpecifier, startRange, takeOptionalSemicolonRange())
    }
    @ReportsErrorDirectly
    private fun parseExportSpecifier(): ImportOrExportSpecifierNode? {
        val name = parseIdentifierName() ?: return reportUnexpectedToken()
        takeIfMatchesKeyword(AS) ?: return ImportOrExportSpecifierNode(name, name)
        val alias = parseIdentifierName() ?: return reportUnexpectedToken()
        return ImportOrExportSpecifierNode(name, alias)
    }
    @ReportsErrorDirectly
    private fun parseExportDeclaration(): ExportDeclarationNode? {
        val startRange = takeIfMatchesKeyword(EXPORT)?.range ?: return null

        if (takeIfMatchesKeyword(DEFAULT) != null) {
            val expr = parseAssignment() ?: return null
            return DefaultExportDeclarationNode(expr, startRange, takeOptionalSemicolonRange())
        }

        return when (currToken.type) {
            MULTIPLY -> {
                advance()
                if (takeIfMatchesKeyword(AS) != null) {
                    val binding = parseIdentifierName() ?: return null
                    val moduleSpecifier = parseFromClause() ?: return null
                    NamespaceExportDeclarationNode(binding, moduleSpecifier, startRange, takeOptionalSemicolonRange())
                } else {
                    val moduleSpecifier = parseFromClause() ?: return null
                    AllExportDeclarationNode(moduleSpecifier, startRange, takeOptionalSemicolonRange())
                }
            }
            LEFT_BRACE -> { // NamedExports
                advance()
                val specifiers = mutableListOf<ImportOrExportSpecifierNode>()
                var skippedComma = true
                while (currToken.type != RIGHT_BRACE) {
                    if (!skippedComma) return reportUnexpectedToken()
                    val specifier = parseExportSpecifier() ?: return null
                    specifiers += specifier
                    skippedComma = skip(COMMA)
                }
                val endRange = expect(RIGHT_BRACE)?.range ?: neverHappens()
                if (takeIfMatchesKeyword(FROM) != null) {
                    val moduleSpecifier = parseStringLiteral() ?: return null
                    return NamedExportDeclarationNode(specifiers.toList(), moduleSpecifier, startRange, endRange, takeOptionalSemicolonRange())
                }
                NamedExportDeclarationNode(specifiers.toList(), null, startRange, endRange, takeOptionalSemicolonRange())
            }
            else ->
                run {
                    val declaration = parseDeclaration() ?: return@run null
                    NamedSingleExportDeclarationNode(declaration, startRange, declaration.range)
                } ?: reportUnexpectedToken()
        }
    }
    /**
     * Parses [ModuleItem](https://tc39.es/ecma262/multipage/ecmascript-language-scripts-and-modules.html#prod-ModuleItem).
     */
    @Careful(false)
    fun parseModuleItem() =
        listOf(
            ::parseImportDeclaration,
            ::parseExportDeclaration,
            ::parseStatementListItem,
        )
            .foldElvisIfHasNoError()
    @ReportsErrorDirectly
    fun parseModule(): ModuleNode? {
        val statements = mutableListOf<StatementNode>()

        while (currToken.type != EOS) {
            if (!isLastStatementTerminated) return reportUnexpectedToken()
            isLastStatementTerminated = false
            val statement = parseModuleItem() ?: return null
            statements += statement
            isLastStatementTerminated = isLastStatementTerminated || currToken.isPrevLineTerminator
        }

        return ModuleNode(statements)
    }
}

private fun List<IdentifierNode>.findDuplicateBoundName(rawNames: List<String>/* to reduce cost */): IdentifierNode? =
    this.asSequence()
        .filterIndexed { i, it ->
            // a, b, a
            //       ^ error
            // we need to find second occurrence of a name
            // and first occurrence cannot have duplicate name before self
            it.value in rawNames.take(i)
        }
        .firstOrNull()
private fun List<IdentifierNode>.toRawNames() =
    map { it.value }

private val ExpressionNode.isLeftHandSide get() =
    this !is UnaryExpressionNode && this !is BinaryExpressionNode && this !is IfExpressionNode
/**
 * Returns whether the identifier is [Identifier](https://tc39.es/ecma262/multipage/ecmascript-language-expressions.html#prod-Identifier).
 */
private fun IdentifierNode.isIdentifier() =
    ReservedWord.values()
        .asSequence()
        .filter { it.not { isContextual } }
        .none { this.isKeyword(it) }
private fun IdentifierNode.isKeyword(keyword: ReservedWord) =
    value == keyword.value
private fun Token.isKeyword(keyword: ReservedWord, verifiedTokenType: Boolean = false) =
    (verifiedTokenType || type == IDENTIFIER) && rawContent == keyword.value
/**
 * Represents whether the ASI can insert semicolon if the token is presented as current token.
 */
private val TokenType.isAsiJob get() =
    this.isOneOf(EOS, RIGHT_BRACE)

private fun neverHappens(): Nothing =
    throw KotlinError("This can never happen")
