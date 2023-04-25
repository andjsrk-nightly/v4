package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.error.Error
import io.github.andjsrk.v4.error.SyntaxError
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.node.literal.*
import io.github.andjsrk.v4.parse.node.literal.`object`.ObjectLiteralNode
import io.github.andjsrk.v4.tokenize.Tokenizer
import org.junit.jupiter.api.Test
import kotlin.test.*

internal class ParserTest {
    @Test
    fun testPrimitiveLiteral() {
        """
            123
            'abc'
            "a'"
            "a\n2"
            null
            true
        """.shouldBeValidAndAlso {
            val exprs = statements.map { it.unwrapExprStmt<PrimitiveLiteralNode>() }
            assert(exprs.size == 6)

            exprs[0].assertTypeThenRun<NumberLiteralNode> {
                assert(value == 123.0)
            }
            exprs[1].assertTypeThenRun<StringLiteralNode> {
                assert(value == "abc")
            }
            exprs[2].assertTypeThenRun<StringLiteralNode> {
                assert(value == "a'")
            }
            exprs[3].assertTypeThenRun<StringLiteralNode> {
                assert(value == "a\n2")
            }
            assertIs<NullLiteralNode>(exprs[4])
            exprs[5].assertTypeThenRun<BooleanLiteralNode> {
                assertTrue(value)
            }
        }
    }
    @Test
    fun testArrayLiteral() {
        """
            [123, "abc", []]
        """.shouldBeValidAndAlso {
            statements[0].unwrapExprStmt<ArrayLiteralNode>().run {
                assert(items.size == 3)
                val exprs = items.map { it.expression }

                assertIs<NumberLiteralNode>(exprs[0])
                assertIs<StringLiteralNode>(exprs[1])
                assertIs<ArrayLiteralNode>(exprs[2])
            }
        }
        """
            [1,]
        """.shouldBeValidAndAlso {
            statements[0].unwrapExprStmt<ArrayLiteralNode>().run {
                assert(items.size == 1)
            }
        }
        """
            [1,,2]
        """.shouldBeInvalidWithError(SyntaxError.UNEXPECTED_TOKEN, ",")
        """
            [,]
        """.shouldBeInvalidWithError(SyntaxError.UNEXPECTED_TOKEN, ",")
    }
    @Test
    fun testDistinguishBetweenObjectLiteralAndBlockStatement() {
        """
            {}
            [{}]
        """.shouldBeValidAndAlso {
            assertIs<BlockStatementNode>(statements[0])

            val obj = statements[1]
                .unwrapExprStmt<ArrayLiteralNode>()
                .items[0]
                .expression
            assertIs<ObjectLiteralNode>(obj)
        }
    }
    @Test
    fun testMemberExpression() {
        """
            a.b
            a?.b
            a[b]
            a?.[b]
            a?.b.c
            a?.b?.[c]
        """.shouldBeValidAndAlso {
            val memberExprs = statements.map { it.unwrapExprStmt<MemberExpressionNode>() }
            assert(memberExprs.size == 6)

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
                assert(!isComputed && !isOptionalChain)
                `object`.run {
                    assertIs<MemberExpressionNode>(this)
                    assert(!isComputed && isOptionalChain)
                    `object`.assertIdentifierNamed("a")
                    property.assertIdentifierNamed("b")
                }
                property.assertIdentifierNamed("c")
            }
            memberExprs[5].run {
                assert(isComputed && isOptionalChain)
                `object`.run {
                    assertIs<MemberExpressionNode>(this)
                    assert(!isComputed && isOptionalChain)
                    `object`.assertIdentifierNamed("a")
                    property.assertIdentifierNamed("b")
                }
                property.assertIdentifierNamed("c")
            }
        }
    }
    @Test
    fun testNormalCallExpression() {
        """
            a()
            a.b(1, ...a)
            a?.(1)
        """.shouldBeValidAndAlso {
            val calls = statements.map { it.unwrapExprStmt<NormalCallExpressionNode>() }
            assert(calls.size == 3)

            calls[0].run {
                callee.assertIdentifierNamed("a")
                assert(arguments.isEmpty())
                assert(!isOptionalChain)
            }
            calls[1].run {
                assertIs<MemberExpressionNode>(callee)
                assert(arguments.size == 2)
                assert(!isOptionalChain)
            }
            calls[2].run {
                callee.assertIdentifierNamed("a")
                assert(arguments.size == 1)
                assert(isOptionalChain)
            }
        }
    }
    @Test
    fun testInvalidCall() {
        """
            super?.()
        """.shouldBeInvalidWithError(SyntaxError.UNEXPECTED_SUPER)

        """
            import?.()
        """.shouldBeInvalidWithError(SyntaxError.UNEXPECTED_TOKEN, "?.")
    }
    @Test
    fun testNormalCallExpressionMixedWithMemberExpression() {
        """
            a().b
            a?.().b
        """.shouldBeValidAndAlso {
            val exprs = statements.map { it.unwrapExprStmt<MemberExpressionNode>() }
            assert(exprs.size == 2)

            exprs[0].run {
                `object`.assertTypeThenRun<NormalCallExpressionNode> {
                    callee.assertIdentifierNamed("a")
                }
                property.assertIdentifierNamed("b")
            }
            exprs[1].run {
                `object`.assertTypeThenRun<NormalCallExpressionNode> {
                    callee.assertIdentifierNamed("a")
                    assert(isOptionalChain)
                }
                property.assertIdentifierNamed("b")
            }
        }
    }
    @Test
    fun testNewExpression() {
        """
            new A()
            new A.B(1, ...a)
        """.shouldBeValidAndAlso {
            val newExprs = statements.map { it.unwrapExprStmt<NewExpressionNode>() }
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
            }
        }
    }
    @Test
    fun testUpdateExpression() {
        fun ExpressionNode.assertUpdateExprThenRun(block: UpdateExpressionNode.() -> Unit) =
            assertTypeThenRun(block)

        """
            a++
            
            ++a
            
            a
            ++
            b
            
            a
            ++b
        """.shouldBeValidAndAlso {
            val exprs = statements.map { it.unwrapExprStmt<ExpressionNode>() }

            exprs[0].assertUpdateExprThenRun {
                assert(!isPrefixed)
            }

            exprs[1].assertUpdateExprThenRun {
                assert(isPrefixed)
            }

            exprs[2].assertIdentifierNamed("a")
            exprs[3].assertUpdateExprThenRun {
                assert(isPrefixed)
            }

            exprs[4].assertIdentifierNamed("a")
            exprs[5].assertUpdateExprThenRun {
                assert(isPrefixed)
            }
        }
    }
}

private inline fun <reified Expr: ExpressionNode> StatementNode.unwrapExprStmt() =
    run {
        assertIs<ExpressionStatementNode>(this)
        assertIs<Expr>(expression)
        expression as Expr
    }
private fun Node.assertIdentifierNamed(name: String) {
    assertTypeThenRun<IdentifierNode> {
        assert(value == name)
    }
}
private inline fun <reified T> Any?.assertTypeThenRun(block: T.() -> Unit) {
    assertIs<T>(this)
    run(block)
}
private fun Parser.parseProgramSuccessfully() =
    parseProgram().let {
        assert(hasError.not())
        assertNotNull(it)
        it
    }
private fun Code.shouldBeValidAndAlso(block: ProgramNode.() -> Unit) {
    block(
        createParser(this)
            .parseProgramSuccessfully()
    )
}
private fun Code.shouldBeInvalidWithError(kind: Error, vararg args: String, range: Range? = null) {
    val (error, errorArgs) = createParser(this).parseProgramUnsuccessfully()
    assert(error.kind == kind)
    if (range != null) assert(error.range == range)
    assert(errorArgs.contentEquals(args))
}
private fun Parser.parseProgramUnsuccessfully() =
    run {
        val program = parseProgram()
        assertNull(program)
        assert(hasError)
        error!! to errorArgs
    }
private fun createParser(code: Code) =
    Parser(Tokenizer(code.trimIndent()))

private typealias Code = String
