package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.BinaryOperationType
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.UnaryOperationType
import io.github.andjsrk.v4.error.Error
import io.github.andjsrk.v4.error.SyntaxError
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.node.ArrayLiteralNode
import io.github.andjsrk.v4.parse.node.ObjectLiteralNode
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
        """.shouldBeValidProgramAnd {
            val exprs = statements.map { it.unwrapExprStmt<PrimitiveLiteralNode>() }
            assert(exprs.size == 6)

            exprs[0].assertTypeThen<NumberLiteralNode> {
                assert(value == 123.0)
            }
            exprs[1].assertTypeThen<StringLiteralNode> {
                assert(value == "abc")
            }
            exprs[2].assertTypeThen<StringLiteralNode> {
                assert(value == "a'")
            }
            exprs[3].assertTypeThen<StringLiteralNode> {
                assert(value == "a\n2")
            }
            assertIs<NullLiteralNode>(exprs[4])
            exprs[5].assertTypeThen<BooleanLiteralNode> {
                assertTrue(value)
            }
        }
    }
    @Test
    fun testArrayLiteral() {
        """
            [123, "abc", []]
        """.shouldBeValidExpressionAnd<ArrayLiteralNode> {
            assert(elements.size == 3)
            val exprs = elements.map { it.expression }

            assertIs<NumberLiteralNode>(exprs[0])
            assertIs<StringLiteralNode>(exprs[1])
            assertIs<ArrayLiteralNode>(exprs[2])
        }

        """
            [1,]
        """.shouldBeValidExpressionAnd<ArrayLiteralNode> {
            assert(elements.size == 1)
        }

        """
            [1,,2]
        """.shouldBeInvalidExpressionWithError(SyntaxError.UNEXPECTED_TOKEN, arrayOf(","))

        """
            [,]
        """.shouldBeInvalidExpressionWithError(SyntaxError.UNEXPECTED_TOKEN, arrayOf(","))
    }
    @Test
    fun testObjectLiteral() {
        """
            {}
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            assert(elements.isEmpty())
        }

        """
            { a: 1, b, ...c }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {

        }

        """
            { a, }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {

        }
    }
    @Test
    fun testDistinguishBetweenObjectLiteralAndBlockStatement() {
        """
            {}
            [{}]
        """.shouldBeValidProgramAnd {
            assertIs<BlockStatementNode>(statements[0])

            val obj = statements[1]
                .unwrapExprStmt<ArrayLiteralNode>()
                .elements[0]
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
        """.shouldBeValidProgramAnd {
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
    fun testOrdinaryCall() {
        """
            a()
            a.b(1, ...a)
            a?.(1)
        """.shouldBeValidProgramAnd {
            val calls = statements.map { it.unwrapExprStmt<OrdinaryCallNode>() }
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
        """.shouldBeInvalidProgramWithError(SyntaxError.UNEXPECTED_SUPER)

        """
            import?.()
        """.shouldBeInvalidProgramWithError(SyntaxError.UNEXPECTED_TOKEN, arrayOf("?."))
    }
    @Test
    fun testOrdinaryCallMixedWithMemberExpression() {
        """
            a().b
            a?.().b
        """.shouldBeValidProgramAnd {
            val exprs = statements.map { it.unwrapExprStmt<MemberExpressionNode>() }
            assert(exprs.size == 2)

            exprs[0].run {
                `object`.assertTypeThen<OrdinaryCallNode> {
                    callee.assertIdentifierNamed("a")
                }
                property.assertIdentifierNamed("b")
            }
            exprs[1].run {
                `object`.assertTypeThen<OrdinaryCallNode> {
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
        """.shouldBeValidProgramAnd {
            val newExprs = statements.map { it.unwrapExprStmt<NewExpressionNode>() }
            assert(newExprs.size == 2)

            newExprs[0].run {
                callee.assertIdentifierNamed("A")
                assert(arguments.isEmpty())
            }
            newExprs[1].run {
                callee.assertTypeThen<MemberExpressionNode> {
                    `object`.assertIdentifierNamed("A")
                    property.assertIdentifierNamed("B")
                }
                assert(arguments.size == 2)
            }
        }
    }
    @Test
    fun testUpdate() {
        fun ExpressionNode.assertUpdateExprThenRun(block: UpdateNode.() -> Unit) =
            assertTypeThen(block)

        """
            a++
            
            ++a
            
            a
            ++
            b
            
            a
            ++b
        """.shouldBeValidProgramAnd {
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
    @Test
    fun testUnaryExpression() {
        """
            void 0
            typeof ++a
            typeof typeof a
        """.shouldBeValidProgramAnd {
            val exprs = statements.map { it.unwrapExprStmt<UnaryExpressionNode>() }

            exprs[0].run {
                assert(operation == UnaryOperationType.VOID)
                assertTrue(isPrefixed)
                assertIs<NumberLiteralNode>(operand)
            }
            exprs[1].run {
                assert(operation == UnaryOperationType.TYPEOF)
                assertIs<UpdateNode>(operand)
            }
        }
    }
    @Test
    fun testExponentiation() {
        """
            ++a ** 1
        """.shouldBeValidExpressionAnd<BinaryExpressionNode> {
            assert(operation == BinaryOperationType.EXPONENTIAL)
            assertIs<UpdateNode>(left)
        }

        """
            -1 ** 1
        """.shouldBeInvalidProgramWithError(SyntaxError.UNEXPECTED_TOKEN_UNARY_EXPONENTIATION)
    }
    @Test
    fun testMultiplication() {
        """
            1 ** 1 * 1 ** 1
        """.shouldBeValidExpressionAnd<BinaryExpressionNode> {
            assert(operation == BinaryOperationType.MULTIPLY)
            arrayOf(left, right).forEach {
                assertIs<BinaryExpressionNode>(it)
                assert(it.operation == BinaryOperationType.EXPONENTIAL)
            }
        }
    }
    @Test
    fun testCoalesce() {
        """
            a | a ?? a
        """.shouldBeValidExpressionAnd<BinaryExpressionNode> {
            assert(operation == BinaryOperationType.COALESCE)
            assertIs<BinaryExpressionNode>(left)
        }

        """
            a && a ?? a
        """.shouldBeInvalidExpressionWithError(SyntaxError.UNEXPECTED_TOKEN, arrayOf("??"))
    }
    @Test
    fun testConditionalExpression() {
        """
            ++a ? a = a : a = a
        """.shouldBeValidExpressionAnd<ConditionalExpressionNode> {
            assertIs<UpdateNode>(test)
            assertIs<BinaryExpressionNode>(consequent)
            assertIs<BinaryExpressionNode>(alternative)
        }
    }
    @Test
    fun testArrowFunction() {
        /**
         * Note: async cases must be second statement.
         */
        fun ProgramNode.forBothSyncAndAsync(block: ArrowFunctionNode.(Boolean) -> Unit) {
            (0..1).forEach {
                val isAsyncCase = it == 1

                block(statements[it].unwrapExprStmt(), isAsyncCase)
            }
        }

        """
            () => 0
            async () => 0
        """.shouldBeValidProgramAnd {
            forBothSyncAndAsync { isAsyncCase ->
                assert(isAsyncCase == isAsync && !isGenerator)
                assert(parameters.isEmpty())
                assertIs<NumberLiteralNode>(body)
            }
        }

        """
            () => {
                0
            }
            async () => {
                0
            }
        """.shouldBeValidProgramAnd {
            forBothSyncAndAsync {
                body.assertTypeThen<BlockStatementNode> {
                    assert(statements.size == 1)
                }
            }
        }

        """
            x => 0
            async x => 0
        """.shouldBeValidProgramAnd {
            forBothSyncAndAsync { isAsyncCase ->
                assert(isAsyncCase == isAsync)
                assert(parameters.size == 1)
            }
        }

        """
            (a, ...b) => 0
        """.shouldBeValidExpressionAnd<ArrowFunctionNode> {
            assert(parameters.size == 2)
            parameters[0].unwrapNonRest<IdentifierNode>()
            parameters[1].unwrapRest<IdentifierNode>()
        }

        """
            (...a, b) => 0
        """.shouldBeInvalidProgramWithError(SyntaxError.ELEMENT_AFTER_REST)

        """
            ([a, b = 1]) => 0
            async ([a, b = 1]) => 0
        """.shouldBeValidProgramAnd {
            forBothSyncAndAsync {
                assert(parameters.size == 1)
                parameters[0].unwrapNonRest<ArrayBindingPatternNode>().run {
                    assert(elements.size == 2)
                    elements[0].unwrapNonRest<IdentifierNode>()
                    elements[1].assertTypeThen<NonRestNode> {
                        assertIs<IdentifierNode>(`as`)
                        assertIs<NumberLiteralNode>(default)
                    }
                }
            }
        }

        """
            ([0]) => 0
        """.shouldBeInvalidExpressionWithError(SyntaxError.INVALID_DESTRUCTURING_TARGET)

        """
            async ([0]) => 0
        """.shouldBeInvalidExpressionWithError(SyntaxError.INVALID_DESTRUCTURING_TARGET)

        """
            ([]) => 0
            async ([]) => 0
        """.shouldBeValidProgramAnd {
            forBothSyncAndAsync {
                assert(
                    parameters[0].unwrapNonRest<ArrayBindingPatternNode>()
                        .elements.isEmpty()
                )
            }
        }

        """
            ([[[a]]]) => 0
            async ([[[a]]]) => 0
        """.shouldBeValidProgramAnd {
            forBothSyncAndAsync {
                parameters[0].unwrapNonRest<ArrayBindingPatternNode>()
                    .elements[0].unwrapNonRest<ArrayBindingPatternNode>()
                    .elements[0].unwrapNonRest<ArrayBindingPatternNode>()
                    .elements[0].`as`.assertIdentifierNamed("a")
            }
        }

        """
            (...{ a }) => 0
            async (...{ a }) => 0
        """.shouldBeValidProgramAnd {
            forBothSyncAndAsync {
                parameters[0].unwrapRest<ObjectBindingPatternNode>()
                    .elements[0].unwrapNonRest<IdentifierNode>()
            }
        }

        """
            ({ ab: a, b = 1, c, ...d }) => 0
            async ({ ab: a, b = 1, c, ...d }) => 0
        """.shouldBeValidProgramAnd {
            forBothSyncAndAsync {
                assert(parameters.size == 1)
                parameters[0].unwrapNonRest<ObjectBindingPatternNode>().run {
                    assert(elements.size == 4)
                    elements[0].assertTypeThen<NonRestObjectPropertyNode> {
                        key.assertIdentifierNamed("ab")
                        `as`.assertIdentifierNamed("a")
                    }
                    elements[1].assertTypeThen<NonRestNode> {
                        `as`.assertIdentifierNamed("b")
                        assertIs<NumberLiteralNode>(default)
                    }
                    elements[2].assertTypeThen<NonRestObjectPropertyNode> {
                        `as`.assertIdentifierNamed("c")
                        assert(`as` == key)
                    }
                    elements[3].unwrapRest<IdentifierNode>()
                }
            }
        }

        """
            ()
            => 0
        """.shouldBeInvalidProgramWithError(SyntaxError.UNEXPECTED_TOKEN, arrayOf("=>"))

        """
            ({ ...{} }) => 0
        """.shouldBeInvalidExpressionWithError(SyntaxError.INVALID_REST_BINDING_PATTERN)

        """
            *() => 0
            async *() => 0
        """.shouldBeValidProgramAnd {
            forBothSyncAndAsync { isAsyncCase ->
                assert(isAsyncCase == isAsync && isGenerator)
            }
        }
    }
    @Test
    fun testAssignment() {
        """
            a = a = a
        """.shouldBeValidExpressionAnd<BinaryExpressionNode> {
            assert(operation == BinaryOperationType.ASSIGN)
            assertTypeThen<BinaryExpressionNode> {
                assert(operation == BinaryOperationType.ASSIGN)
            }
        }
    }
}

private inline fun <reified T: Node> MaybeRestNode.unwrapNonRest() =
    run {
        assertIs<NonRestNode>(this)
        `as` as T
    }
private inline fun <reified T: Node> MaybeRestNode.unwrapRest() =
    run {
        assertIs<RestNode>(this)
        `as` as T
    }
private inline fun <reified Expr: ExpressionNode> StatementNode.unwrapExprStmt() =
    run {
        assertIs<ExpressionStatementNode>(this)
        assertIs<Expr>(expression)
        expression as Expr
    }
private fun Node.assertIdentifierNamed(name: String) {
    assertTypeThen<IdentifierNode> {
        assert(value == name)
    }
}
private inline fun <reified T> Any?.assertTypeThen(block: T.() -> Unit) {
    assertIs<T>(this)
    run(block)
}
private inline fun <N: Node> Code.shouldBeValidAnd(parseFn: Parser.() -> N?, block: N.() -> Unit) {
    block(
        createParser(this)
            .parseSuccessfully(parseFn)
    )
}
private inline fun <reified Expr: ExpressionNode> Code.shouldBeValidExpressionAnd(block: Expr.() -> Unit) =
    shouldBeValidAnd(Parser::parseExpression as Parser.() -> Expr, block)
private inline fun Code.shouldBeValidProgramAnd(block: ProgramNode.() -> Unit) =
    shouldBeValidAnd(Parser::parseProgram, block)
private fun Code.shouldBeInvalidWithError(parseFn: Parser.() -> Node?, kind: Error, args: Array<String>? = null, range: Range? = null) {
    val (error, errorArgs) = createParser(this).parseUnsuccessfully(parseFn)
    assert(error.kind == kind) { "Actual error was: $error" }
    if (range != null) assert(error.range == range)
    if (args != null) assert(errorArgs.contentEquals(args))
}
private fun Code.shouldBeInvalidExpressionWithError(kind: Error, args: Array<String>? = null, range: Range? = null) =
    shouldBeInvalidWithError(Parser::parseExpression, kind, args, range)
private fun Code.shouldBeInvalidProgramWithError(kind: Error, args: Array<String>? = null, range: Range? = null) =
    shouldBeInvalidWithError(Parser::parseProgram, kind, args, range)
private inline fun <N: Node> Parser.parseSuccessfully(parseFn: Parser.() -> N?) =
    parseFn().let {
        assert(!hasError) { "Error occurred: $error" }
        assertNotNull(it)
        it
    }
private inline fun Parser.parseUnsuccessfully(parseFn: Parser.() -> Node?) =
    run {
        assertNull(parseFn())
        assert(hasError)
        error!! to errorArgs
    }
private fun createParser(code: Code) =
    Parser(Tokenizer(code.trimIndent()))

private typealias Code = String
