package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.ErrorWithRange
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.WasSuccessful
import io.github.andjsrk.v4.error.Error
import io.github.andjsrk.v4.error.SyntaxError
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.node.literal.ArrayLiteralNode
import io.github.andjsrk.v4.parse.node.literal.BigintLiteralNode
import io.github.andjsrk.v4.parse.node.literal.NumericLiteralNode
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
    private fun advance() {
        currToken = tokenizer.getNextToken()
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
    private fun reportUnexpectedToken(expected: TokenType, range: Range) {
        val kind = when (expected) {
            EOS -> SyntaxError.UNEXPECTED_EOS
            NUMBER, BIGINT -> SyntaxError.UNEXPECTED_TOKEN_NUMBER
            STRING -> SyntaxError.UNEXPECTED_TOKEN_STRING
            IDENTIFIER -> SyntaxError.UNEXPECTED_TOKEN_IDENTIFIER
            TEMPLATE_HEAD, TEMPLATE_MIDDLE, TEMPLATE_TAIL, TEMPLATE_FULL -> SyntaxError.UNEXPECTED_TEMPLATE_STRING
            ILLEGAL -> SyntaxError.INVALID_OR_UNEXPECTED_TOKEN
            else -> return reportErrorMessage(
                SyntaxError.UNEXPECTED_TOKEN,
                range,
                expected.staticContent ?: expected.name,
            )
        }
        reportErrorMessage(kind, range)
    }
    private fun expect(tokenType: TokenType, check: (Token) -> Boolean = { true }) {
        if (currToken.type == tokenType && check(currToken)) advance()
        else reportUnexpectedToken(tokenType, currToken.range)
    }
    private fun parseExpressionWithoutContinuous(): ExpressionNode? {
        return when (currToken.type) {
            IDENTIFIER -> {
                val content = currToken.rawContent
                advance()
                IdentifierNode(content)
            }
            NUMBER -> NumericLiteralNode(currToken.literal).alsoAdvance()
            BIGINT -> BigintLiteralNode(currToken.literal).alsoAdvance()
            LEFT_BRACK -> {
                advance()
                val array = ArrayLiteralNode.Unsealed()
                while (currToken.type != RIGHT_BRACK) {
                    array.value += parseExpression() ?: return null
                }
                expect(RIGHT_BRACK)
                array.toSealed()
            }
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

        expect(LEFT_BRACE)
        while (currToken.type != RIGHT_BRACE) {
            block.statements += parseStatement() ?: return null
        }
        expect(RIGHT_BRACE)

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
