package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.ErrorWithRange
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.WasSuccessful
import io.github.andjsrk.v4.error.Error
import io.github.andjsrk.v4.error.SyntaxError
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.node.literal.*
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
        currToken = tokenizer.getNextToken()
        return currToken
    }
    private fun <T> T.alsoAdvance() =
        also { advance() }
    private fun skip(tokenType: TokenType): WasSuccessful =
        (currToken.type == tokenType).thenAlso {
            advance()
        }
    private fun reportErrorMessage(kind: Error, range: Range, vararg args: String) {
        error = ErrorWithRange(kind, range)
        errorArgs = arrayOf(*args)
    }
    private fun reportUnexpectedToken(token: Token = currToken) {
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
        reportErrorMessage(kind, token.range)
    }
    private fun expect(tokenType: TokenType, check: (Token) -> Boolean = { true }) =
        if (currToken.type == tokenType && check(currToken)) advance()
        else null.also { reportUnexpectedToken() }
    private fun parseString(): StringLiteralNode? {
        TODO()
    }
    private fun parseArrayLiteral(): ArrayLiteralNode? {
        val array = ArrayLiteralNode.Unsealed()
        array.startToken = expect(LEFT_BRACK) ?: return null


        // allow trailing comma, but disallow sparse array
        var skippedComma = true
        while (currToken.type != RIGHT_BRACK) {
            if (!skippedComma || currToken.type == COMMA) {
                reportUnexpectedToken()
                return null
            }
            array.value += parseExpression() ?: return null
            skippedComma = skip(COMMA)
        }
        array.endToken = expect(RIGHT_BRACK) ?: return null

        return array.toSealed()
    }
    private fun parseObjectLiteral(): ObjectLiteralNode? {
        val obj = ObjectLiteralNode.Unsealed()

        obj.startToken = expect(LEFT_BRACE) ?: return null

        val skippedComma = true
        while (currToken.type != RIGHT_BRACE) {
            if (!skippedComma || currToken.type == COMMA) {
                reportUnexpectedToken()
                return null
            }
            TODO()
        }
        obj.endToken = expect(RIGHT_BRACE) ?: return null

        return obj.toSealed()
    }
    private fun parseExpressionWithoutContinuous(): ExpressionNode? {
        val content = currToken.rawContent
        return when (currToken.type) {
            IDENTIFIER -> when (content) {
                "null" -> NullLiteralNode(currToken)
                "true", "false" -> BooleanLiteralNode(currToken)
                else -> IdentifierNode(currToken)
            }
                .alsoAdvance()
            STRING -> StringLiteralNode(currToken).alsoAdvance()
            NUMBER -> NumericLiteralNode(currToken).alsoAdvance()
            BIGINT -> BigintLiteralNode(currToken).alsoAdvance()
            LEFT_BRACK -> parseArrayLiteral()
            LEFT_BRACE -> parseObjectLiteral()
            else -> null
        }
    }
    private fun parseExpression(): ExpressionNode? {
        val expr = parseExpressionWithoutContinuous()
        return expr // temp
    }
    private fun parseIfStatement(): IfStatementNode? {
        expect(IDENTIFIER) { it.rawContent == "if" }
        val `if` = IfStatementNode.Unsealed()

        expect(LEFT_PAREN)
        `if`.condition = parseExpression() ?: return null
        expect(RIGHT_PAREN)
        `if`.body = parseStatement() ?: return null

        return `if`.toSealed()
    }
    private fun parseBlockStatement(): BlockStatementNode? {
        val block = BlockStatementNode.Unsealed()

        block.startToken = expect(LEFT_BRACE) ?: return null
        while (currToken.type != RIGHT_BRACE) {
            block.statements += parseStatement() ?: return null
        }
        block.endToken = expect(RIGHT_BRACE) ?: return null

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
        val program = ProgramNode.Unsealed()

        while (currToken.type != EOS) {
            program.statements += parseModuleItem() ?: return null
        }

        return program.toSealed()
    }
}
