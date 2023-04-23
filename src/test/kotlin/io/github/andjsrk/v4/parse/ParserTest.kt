package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.node.literal.*
import io.github.andjsrk.v4.parse.node.literal.`object`.ObjectLiteralNode
import io.github.andjsrk.v4.tokenize.Tokenizer
import org.junit.jupiter.api.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull

internal class ParserTest {
    @Test
    fun testPrimitiveLiteral() {
        val parser = createParser("""
            123
            'abc'
            "a'"
            "a\n2"
            null
            true
        """.trimIndent())

        val program = parser.parseProgram()
        assert(parser.hasError.not())
        assertNotNull(program)

        val exprs = program.statements.map { it.unwrapExprStmt<PrimitiveLiteralNode>() }
        assert(exprs.size == 6)

        exprs[0].assertTypeThenRun<NumberLiteralNode> { assert(raw == "123" && value == 123.0) }
        exprs[1].assertTypeThenRun<StringLiteralNode> { assert(raw == "'abc'" && value == "abc") }
        exprs[2].assertTypeThenRun<StringLiteralNode> { assert(raw == "\"a'\"" && value == "a'") }
        exprs[3].assertTypeThenRun<StringLiteralNode> { assert(raw == "\"a\\n2\"" && value == "a\n2") }
        exprs[4].assertTypeThenRun<NullLiteralNode> { assert(raw == "null") }
        exprs[5].assertTypeThenRun<BooleanLiteralNode> { assert(raw == "true" && value) }
    }
    @Test
    fun testArrayLiteral() {
        val parser = createParser(
            """
            [123, "abc", []]
        """.trimIndent()
        )

        val program = parser.parseProgram()
        assert(parser.hasError.not())
        assertNotNull(program)

        program.statements[0].unwrapExprStmt<ArrayLiteralNode>().run {
            assert(items.size == 3)

            items[0].assertTypeThenRun<NumberLiteralNode> { assert(raw == "123") }
            items[1].assertTypeThenRun<StringLiteralNode> { assert(raw == "\"abc\"") }
            items[2].assertTypeThenRun<ArrayLiteralNode> { assert(items.isEmpty()) }
        }
    }
    @Test
    fun testDistinguishBetweenObjectLiteralAndBlockStatement() {
        val parser = createParser("""
            {}
            [{}]
        """.trimIndent())

        val program = parser.parseProgram()
        assert(parser.hasError.not())
        assertNotNull(program)

        assertIs<BlockStatementNode>(program.statements[0])

        val obj = program.statements[1]
            .unwrapExprStmt<ArrayLiteralNode>()
            .items[0]
        assertIs<ObjectLiteralNode>(obj)
    }
    @Test
    fun testMemberExpression() {
        val parser = createParser("""
            a.b
            a?.b
            a[b]
            a?.[b]
            a?.b[c]
        """.trimIndent())

        val program = parser.parseProgram()
        assert(parser.hasError.not())
        assertNotNull(program)

        val memberExprs = program.statements.map { it.unwrapExprStmt<MemberExpressionNode>() }
        assert(memberExprs.size == 5)

        memberExprs[0].run {
            assert(!isComputed && !isOptionalChain)
            `object`.assertIdentifierNamed("a")
            property.assertIdentifierNamed("b")
        }
        memberExprs[1].run {
            assert(!isComputed && isOptionalChain)
            `object`.assertIdentifierNamed("a")
            property.assertIdentifierNamed("b")
        }
        memberExprs[2].run {
            assert(isComputed && !isOptionalChain)
            `object`.assertIdentifierNamed("a")
            property.assertIdentifierNamed("b")
        }
        memberExprs[3].run {
            assert(isComputed && isOptionalChain)
            `object`.assertIdentifierNamed("a")
            property.assertIdentifierNamed("b")
        }
        memberExprs[4].run {
            assert(isComputed && !isOptionalChain)
            `object`.run {
                assertIs<MemberExpressionNode>(this)
                assert(!isComputed && isOptionalChain)
                `object`.assertIdentifierNamed("a")
                property.assertIdentifierNamed("b")
            }
            property.assertIdentifierNamed("c")
        }
    }
    @Test
    fun testNewExpression() {
        val parser = createParser("""
            new A()
            new A.B(1, ...a)
        """.trimIndent())

        val program = parser.parseProgram()
        assert(parser.hasError.not())
        assertNotNull(program)

        val newExprs = program.statements.map { it.unwrapExprStmt<NewExpressionNode>() }
        assert(newExprs.size == 2)

        newExprs[0].run {
            callee.assertIdentifierNamed("A")
            assert(arguments.isEmpty())
        }
        newExprs[1].run {
            callee.assertTypeThenRun<MemberExpressionNode> {
                `object`.assertIdentifierNamed("A")
                property.assertIdentifierNamed("B")
            }
            assert(arguments.size == 2)
            arguments[0].run {
                assert(!isSpread)
                assertIs<NumberLiteralNode>(expression)
            }
            arguments[1].run {
                assert(isSpread)
                assertIs<IdentifierNode>(expression)
            }
        }
    }
}

private inline fun <reified T: ExpressionNode> StatementNode.unwrapExprStmt() =
    run {
        assertIs<ExpressionStatementNode>(this)
        assertIs<T>(expression)
        expression as T
    }
private fun Node.assertIdentifierNamed(name: String) =
    assertTypeThenRun<IdentifierNode> { assert(value == name) }
private inline fun <reified T> Any?.assertTypeThenRun(block: T.() -> Unit) =
    run {
        assertIs<T>(this)
        run(block)
    }
private fun createParser(code: String) =
    Parser(Tokenizer(code))
