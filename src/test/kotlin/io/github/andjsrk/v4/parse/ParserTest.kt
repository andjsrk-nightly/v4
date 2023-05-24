package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.BinaryOperationType
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.UnaryOperationType
import io.github.andjsrk.v4.error.ErrorKind
import io.github.andjsrk.v4.error.SyntaxErrorKind
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.node.ArrayLiteralNode
import io.github.andjsrk.v4.parse.node.ObjectLiteralNode
import io.github.andjsrk.v4.tokenize.Tokenizer
import org.junit.jupiter.api.Test
import kotlin.test.*

internal class ParserTest {
    // <editor-fold desc="expressions">
    @Test
    fun testPrimitiveLiteral() {
        """
            123
        """.shouldBeValidExpressionAnd<NumberLiteralNode> {
            assert(value == 123.0)
        }

        """
            'abc'
        """.shouldBeValidExpressionAnd<StringLiteralNode> {
            assert(value == "abc")
        }

        """
            "a'"
        """.shouldBeValidExpressionAnd<StringLiteralNode> {
            assert(value == "a'")
        }

        """
            "a\n2"
        """.shouldBeValidExpressionAnd<StringLiteralNode> {
            assert(value == "a\n2")
        }

        """
            null
        """.shouldBeValidExpressionAnd<NullLiteralNode> {}

        """
            true
        """.shouldBeValidExpressionAnd<BooleanLiteralNode> {
            assertTrue(value)
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
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.UNEXPECTED_TOKEN, listOf(","))

        """
            [,]
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.UNEXPECTED_TOKEN, listOf(","))
    }
    @Test
    fun testBasicObjectLiteral() {
        """
            {}
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            assert(elements.isEmpty())
        }

        """
            { a: 1, b, ...c }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            assert(elements.size == 3)
            elements[0].assertTypeThen<PropertyNode> {
                key.assertIdentifierNamed("a")
                assertIs<NumberLiteralNode>(value)
            }
            elements[1].assertTypeThen<PropertyShorthandNode> {
                name.assertIdentifierNamed("b")
            }
            elements[2].assertTypeThen<SpreadNode> {
                expression.assertIdentifierNamed("c")
            }
        }

        """
            { a }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeThen<PropertyShorthandNode> {
                name.assertIdentifierNamed("a")
            }
        }

        """
            { a = 1 }
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.INVALID_COVER_INITIALIZED_NAME)

        """
            { a: [ { a = 1 } ] }
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.INVALID_COVER_INITIALIZED_NAME)
    }
    @Test
    fun testMethodLike() {
        """
            { a() {} }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeThen<ObjectMethodNode> {
                name.assertIdentifierNamed("a")
                assert(parameters.elements.isEmpty())
                assert(!isAsync && !isGenerator)
            }
        }

        """
            { async a() {} }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeThen<ObjectMethodNode> {
                name.assertIdentifierNamed("a")
                assert(isAsync)
            }
        }

        """
            { gen a() {} }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeThen<ObjectMethodNode> {
                name.assertIdentifierNamed("a")
                assert(isGenerator)
            }
        }

        """
            { async gen a() {} }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeThen<ObjectMethodNode> {
                name.assertIdentifierNamed("a")
                assert(isAsync && isGenerator)
            }
        }

        """
            { gen async a() {} }
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.UNEXPECTED_TOKEN_IDENTIFIER)

        """
            { async() {} }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeThen<ObjectMethodNode> {
                name.assertIdentifierNamed("async")
                assert(!isAsync)
            }
        }

        """
            { get a() {} }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeThen<ObjectGetterNode> {
                name.assertIdentifierNamed("a")
            }
        }

        """
            { get a(x) {} }
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.BAD_GETTER_ARITY)

        """
            { set a(x) {} }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeThen<ObjectSetterNode> {
                name.assertIdentifierNamed("a")
                parameter.binding.assertIdentifierNamed("x")
            }
        }

        """
            { set a() {} }
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.BAD_SETTER_ARITY)

        """
            { set a(...x) {} }
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.BAD_SETTER_REST_PARAMETER)

        """
            { async get() {} }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeThen<ObjectMethodNode> {
                name.assertIdentifierNamed("get")
                assert(isAsync)
            }
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
        """.shouldBeValidExpressionAnd<MemberExpressionNode> {
            assert(!isComputed && !isOptionalChain)
            `object`.assertIdentifierNamed("a")
            property.assertIdentifierNamed("b")
        }

        """
            a?.b
        """.shouldBeValidExpressionAnd<MemberExpressionNode> {
            assert(!isComputed && isOptionalChain)
            `object`.assertIdentifierNamed("a")
            property.assertIdentifierNamed("b")
        }

        """
            a[b]
        """.shouldBeValidExpressionAnd<MemberExpressionNode> {
            assert(isComputed && !isOptionalChain)
            `object`.assertIdentifierNamed("a")
            property.assertIdentifierNamed("b")
        }

        """
            a?.[b]
        """.shouldBeValidExpressionAnd<MemberExpressionNode> {
            assert(isComputed && isOptionalChain)
            `object`.assertIdentifierNamed("a")
            property.assertIdentifierNamed("b")
        }

        """
            a?.b.c
        """.shouldBeValidExpressionAnd<MemberExpressionNode> {
            assert(!isComputed && !isOptionalChain)
            `object`.run {
                assertIs<MemberExpressionNode>(this)
                assert(!isComputed && isOptionalChain)
                `object`.assertIdentifierNamed("a")
                property.assertIdentifierNamed("b")
            }
            property.assertIdentifierNamed("c")
        }

        """
            a?.b?.[c]
        """.shouldBeValidExpressionAnd<MemberExpressionNode> {
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
    @Test
    fun testNormalCall() {
        """
            a()
        """.shouldBeValidExpressionAnd<NormalCallNode> {
            callee.assertIdentifierNamed("a")
            assert(arguments.elements.isEmpty())
            assert(!isOptionalChain)
        }

        """
            a.b(1, ...a)
        """.shouldBeValidExpressionAnd<NormalCallNode> {
            assertIs<MemberExpressionNode>(callee)
            assert(arguments.elements.size == 2)
            assert(!isOptionalChain)
        }

        """
            a?.(1)
        """.shouldBeValidExpressionAnd<NormalCallNode> {
            callee.assertIdentifierNamed("a")
            assert(arguments.elements.size == 1)
            assert(isOptionalChain)
        }
    }
    @Test
    fun testInvalidCall() {
        """
            super?.()
        """.shouldBeInvalidProgramWithError(SyntaxErrorKind.UNEXPECTED_SUPER)

        """
            import?.()
        """.shouldBeInvalidProgramWithError(SyntaxErrorKind.UNEXPECTED_TOKEN, listOf("?."))
    }
    @Test
    fun testOrdinaryCallMixedWithMemberExpression() {
        """
            a().b
        """.shouldBeValidExpressionAnd<MemberExpressionNode> {
            `object`.assertTypeThen<NormalCallNode> {
                callee.assertIdentifierNamed("a")
            }
            property.assertIdentifierNamed("b")
        }

        """
            a?.().b
        """.shouldBeValidExpressionAnd<MemberExpressionNode> {
            `object`.assertTypeThen<NormalCallNode> {
                callee.assertIdentifierNamed("a")
                assert(isOptionalChain)
            }
            property.assertIdentifierNamed("b")
        }
    }
    @Test
    fun testNewExpression() {
        """
            new A()
        """.shouldBeValidExpressionAnd<NewExpressionNode> {
            callee.assertIdentifierNamed("A")
            assert(arguments.elements.isEmpty())
        }

        """
            new A.B(1, ...a)
        """.shouldBeValidExpressionAnd<NewExpressionNode> {
            callee.assertTypeThen<MemberExpressionNode> {
                `object`.assertIdentifierNamed("A")
                property.assertIdentifierNamed("B")
            }
            assert(arguments.elements.size == 2)
        }
    }
    @Test
    fun testUpdate() {
        """
            a++
        """.shouldBeValidExpressionAnd<UpdateNode> {
            assert(!isPrefixed)
        }

        """
            ++a
        """.shouldBeValidExpressionAnd<UpdateNode> {
            assert(isPrefixed)
        }

        """
            a
            ++
            b
        """.shouldBeValidProgramAnd {
            val exprs = statements.map { it.unwrapExprStmt<ExpressionNode>() }

            exprs[0].assertIdentifierNamed("a")
            exprs[1].assertTypeThen<UpdateNode> {
                assert(isPrefixed)
            }
        }

        """
            a
            ++b
        """.shouldBeValidProgramAnd {
            val exprs = statements.map { it.unwrapExprStmt<ExpressionNode>() }

            exprs[0].assertIdentifierNamed("a")
            exprs[1].assertTypeThen<UpdateNode> {
                assert(isPrefixed)
            }
        }
    }
    @Test
    fun testUnaryExpression() {
        """
            void 0
        """.shouldBeValidExpressionAnd<UnaryExpressionNode> {
            assert(operation == UnaryOperationType.VOID)
            assertTrue(isPrefixed)
            assertIs<NumberLiteralNode>(operand)
        }

        """
            typeof ++a
        """.shouldBeValidExpressionAnd<UnaryExpressionNode> {
            assert(operation == UnaryOperationType.TYPEOF)
            assertIs<UpdateNode>(operand)
        }

        """
            typeof typeof a
        """.shouldBeValidExpressionAnd<UnaryExpressionNode> {
            assert(operation == UnaryOperationType.TYPEOF)
            assertIs<UnaryExpressionNode>(operand)
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
        """.shouldBeInvalidProgramWithError(SyntaxErrorKind.UNEXPECTED_TOKEN_UNARY_EXPONENTIATION)
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
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.UNEXPECTED_TOKEN, listOf("??"))
    }
    @Test
    fun testIfExpression() {
        """
            if (true) a = a else a = a
        """.shouldBeValidExpressionAnd<IfExpressionNode> {
            assertIs<BooleanLiteralNode>(test)
            assertIs<BinaryExpressionNode>(then)
            assertIs<BinaryExpressionNode>(`else`)
        }

        """
            if (true) a = a
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.UNEXPECTED_EOS)
    }
    @Test
    fun testArrowFunction() {
        /**
         * Note: async cases must be second statement.
         */
        fun ModuleNode.forBothSyncAndAsync(block: ArrowFunctionNode.(Boolean) -> Unit) {
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
                assert(parameters.elements.isEmpty())
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
                assert(parameters.elements.size == 1)
            }
        }

        """
            (a, ...b) => 0
        """.shouldBeValidExpressionAnd<ArrowFunctionNode> {
            val params = parameters.elements
            assert(params.size == 2)
            params[0].unwrapNonRest<IdentifierNode>()
            params[1].unwrapRest<IdentifierNode>()
        }

        """
            (...a, b) => 0
        """.shouldBeInvalidProgramWithError(SyntaxErrorKind.ELEMENT_AFTER_REST)

        """
            ([a, b = 1]) => 0
            async ([a, b = 1]) => 0
        """.shouldBeValidProgramAnd {
            forBothSyncAndAsync {
                val params = parameters.elements
                assert(params.size == 1)
                params[0].unwrapNonRest<ArrayBindingPatternNode>().run {
                    assert(elements.size == 2)
                    elements[0].unwrapNonRest<IdentifierNode>()
                    elements[1].assertTypeThen<NonRestNode> {
                        assertIs<IdentifierNode>(binding)
                        assertIs<NumberLiteralNode>(default)
                    }
                }
            }
        }

        """
            ([0]) => 0
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.INVALID_DESTRUCTURING_TARGET)

        """
            async ([0]) => 0
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.INVALID_DESTRUCTURING_TARGET)

        """
            ([]) => 0
            async ([]) => 0
        """.shouldBeValidProgramAnd {
            forBothSyncAndAsync {
                assert(
                    parameters.elements[0].unwrapNonRest<ArrayBindingPatternNode>()
                        .elements.isEmpty()
                )
            }
        }

        """
            ([[[a]]]) => 0
            async ([[[a]]]) => 0
        """.shouldBeValidProgramAnd {
            forBothSyncAndAsync {
                parameters.elements[0].unwrapNonRest<ArrayBindingPatternNode>()
                    .elements[0].unwrapNonRest<ArrayBindingPatternNode>()
                    .elements[0].unwrapNonRest<ArrayBindingPatternNode>()
                    .elements[0].binding.assertIdentifierNamed("a")
            }
        }

        """
            (...{ a }) => 0
            async (...{ a }) => 0
        """.shouldBeValidProgramAnd {
            forBothSyncAndAsync {
                parameters.elements[0].unwrapRest<ObjectBindingPatternNode>()
                    .elements[0].unwrapNonRest<IdentifierNode>()
            }
        }

        """
            ({ ab: a, b = 1, c, ...d }) => 0
            async ({ ab: a, b = 1, c, ...d }) => 0
        """.shouldBeValidProgramAnd {
            forBothSyncAndAsync {
                val params = parameters.elements
                assert(params.size == 1)
                params[0].unwrapNonRest<ObjectBindingPatternNode>().run {
                    assert(elements.size == 4)
                    elements[0].assertTypeThen<NonRestObjectPropertyNode> {
                        key.assertIdentifierNamed("ab")
                        binding.assertIdentifierNamed("a")
                    }
                    elements[1].assertTypeThen<NonRestNode> {
                        binding.assertIdentifierNamed("b")
                        assertIs<NumberLiteralNode>(default)
                    }
                    elements[2].assertTypeThen<NonRestObjectPropertyNode> {
                        binding.assertIdentifierNamed("c")
                        assert(binding == key)
                    }
                    elements[3].unwrapRest<IdentifierNode>()
                }
            }
        }

        """
            (a, a) => 0
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.DUPLICATE_PARAMETER_NAMES, range=Range(4, 5))

        """
            (a, [a]) => 0
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.DUPLICATE_PARAMETER_NAMES, range=Range(5, 6))

        """
            ()
            => 0
        """.shouldBeInvalidProgramWithError(SyntaxErrorKind.UNEXPECTED_TOKEN, listOf("=>"))

        """
            (a = await 0) => 0
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.AWAIT_EXPRESSION_FORMAL_PARAMETER)

        """
            (a = yield 0) => 0
        """.shouldBeInvalidProgramWithError(SyntaxErrorKind.YIELD_IN_PARAMETER)

        """
            ({ ...{} }) => 0
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.INVALID_REST_BINDING_PATTERN)

        """
            gen () => 0
            async gen () => 0
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
    // </editor-fold>
    @Test
    fun testLexicalDeclaration() {
        """
            var a = 1
        """.shouldBeValidStatementAnd<LexicalDeclarationNode> {
            assert(kind == LexicalDeclarationKind.VAR)
            binding.assertIdentifierNamed("a")
            assertIs<NumberLiteralNode>(value)
        }

        """
            let a = 1
        """.shouldBeValidStatementAnd<LexicalDeclarationNode> {
            assert(kind == LexicalDeclarationKind.LET)
        }

        """
            var a
        """.shouldBeValidStatementAnd<LexicalDeclarationNode> {
            assertNull(value)
        }

        """
            let a
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.DECLARATION_MISSING_INITIALIZER)

        """
            var {}
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.DECLARATION_MISSING_INITIALIZER)
    }
    @Test
    fun testClass() {
        """
            class {}
        """.shouldBeValidExpressionAnd<ClassExpressionNode> {
            assertNull(name)
        }

        """
            class A {}
        """.shouldBeValidStatementAnd<ClassDeclarationNode> {
            name.assertIdentifierNamed("A")
        }

        """
            class {}
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.MISSING_CLASS_NAME)

        """
            class A {
                a = 0
            }
        """.shouldBeValidStatementAnd<ClassDeclarationNode> {
            elements[0].assertTypeThen<FieldNode> {
                assert(!isStatic)
                name.assertIdentifierNamed("a")
                assertIs<NumberLiteralNode>(value)
            }
        }

        """
            class A {
                a
            }
        """.shouldBeValidStatementAnd<ClassDeclarationNode> {
            elements[0].assertTypeThen<FieldNode> {
                assertNull(value)
            }
        }

        """
            class A {
                static a = 0
            }
        """.shouldBeValidStatementAnd<ClassDeclarationNode> {
            elements[0].assertTypeThen<FieldNode> {
                assert(isStatic)
            }
        }

        """
            class A {
                a() {}
            }
        """.shouldBeValidStatementAnd<ClassDeclarationNode> {
            assertIs<ClassMethodNode>(elements[0])
        }

        """
            class A {
                if = 0
            }
        """.shouldBeValidStatementAnd<ClassDeclarationNode> {
            elements[0].assertTypeThen<FieldNode> {
                name.assertIdentifierNamed("if")
            }
        }

        """
            class A {
                constructor() {}
            }
        """.shouldBeValidStatementAnd<ClassDeclarationNode> {
            assertNotNull(constructor)
        }

        """
            class A extends null {
                constructor() {
                    super()
                }
            }
        """.shouldBeValidStatementAnd<ClassDeclarationNode> {
            assertIs<NullLiteralNode>(parent)
            val constructor = constructor
            assertNotNull(constructor)
            assert(constructor.body.contains(SuperCallNode::class))
        }

        """
            class A {
                constructor() {
                    super()
                }
            }
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.UNEXPECTED_SUPER)
    }
    @Test
    fun testIfStatement() {
        """
            if (true);
        """.shouldBeValidStatementAnd<IfStatementNode> {
            assertIs<BooleanLiteralNode>(test)
            assertIs<EmptyStatementNode>(then)
        }

        """
            if (true);
            else;
        """.shouldBeValidStatementAnd<IfStatementNode> {
            assertIs<EmptyStatementNode>(then)
            assertIs<EmptyStatementNode>(`else`)
        }

        """
            if (true) var a
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.UNEXPECTED_TOKEN_IDENTIFIER)
    }
    @Test
    fun testFor() {
        """
            for (var i = 0; i < 5; i++);
        """.shouldBeValidStatementAnd<NormalForNode> {
            assertIs<LexicalDeclarationNode>(init)
            assertIs<BinaryExpressionNode>(test)
            assertIs<UpdateNode>(update)
            assertIs<EmptyStatementNode>(body)
        }

        """
            for ();
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.UNEXPECTED_TOKEN)

        """
            for (;);
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.UNEXPECTED_TOKEN)

        """
            for (;;);
        """.shouldBeValidStatementAnd<NormalForNode> {
            assertNull(init)
            assertNull(test)
            assertNull(update)
        }

        """
            for (let elem in [1, 2]);
        """.shouldBeValidStatementAnd<ForInNode> {
            assert(declaration.kind == LexicalDeclarationKind.LET)
        }

        """
            for (let elem = 0 in [1, 2]);
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.UNEXPECTED_TOKEN)

        """
            for (a in [1, 2]);
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.UNEXPECTED_TOKEN_IDENTIFIER)
    }
    @Test
    fun testWhile() {
        """
            while (true);
        """.shouldBeValidStatementAnd<WhileNode> {
            assert(!atLeastOnce)
            assertIs<BooleanLiteralNode>(test)
            assertIs<EmptyStatementNode>(body)
        }

        """
            while+ (true);
        """.shouldBeValidStatementAnd<WhileNode> {
            assert(atLeastOnce)
            assertIs<BooleanLiteralNode>(test)
            assertIs<EmptyStatementNode>(body)
        }
    }
    @Test
    fun testIterationFlowControlStatement() {
        """
            for (;;) continue
        """.shouldBeValidStatementAnd<NormalForNode> {
            assertIs<ContinueNode>(body)
        }

        """
            for (;;) break
        """.shouldBeValidStatementAnd<NormalForNode> {
            assertIs<BreakNode>(body)
        }

        """
            break
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.UNEXPECTED_TOKEN_IDENTIFIER/* temp */)
    }
}

private inline fun <reified T: Node> MaybeRestNode.unwrapNonRest() =
    run {
        assertIs<NonRestNode>(this)
        binding as T
    }
private inline fun <reified T: Node> MaybeRestNode.unwrapRest() =
    run {
        assertIs<RestNode>(this)
        binding as T
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
private inline fun <RN: Node, reified N: Node> Code.shouldBeValidAnd(parseFn: Parser.() -> RN?, block: N.() -> Unit) {
    block(
        createParser(this)
            .parseSuccessfully(parseFn) as N
    )
}
private inline fun <reified Expr: ExpressionNode> Code.shouldBeValidExpressionAnd(block: Expr.() -> Unit) =
    shouldBeValidAnd(Parser::parseExpression, block)
private inline fun <reified Stmt: StatementNode> Code.shouldBeValidStatementAnd(block: Stmt.() -> Unit) =
    shouldBeValidAnd(
        Parser::parseModuleItem,
        block,
    )
private inline fun Code.shouldBeValidProgramAnd(block: ModuleNode.() -> Unit) =
    shouldBeValidAnd(Parser::parseModule, block)
private fun Code.shouldBeInvalidWithError(parseFn: Parser.() -> Node?, kind: ErrorKind, args: List<String>? = null, range: Range? = null) {
    val error = createParser(this).parseUnsuccessfully(parseFn)
    assert(error.kind == kind) {
        """
            Expected: $kind
            Actual: $error
        """.trimIndent()
    }
    if (range != null) assert(error.range == range)
    if (args != null) {
        val errorArgs = error.args
        assertNotNull(errorArgs)
        assert(errorArgs == args)
    }
}
private fun Code.shouldBeInvalidExpressionWithError(kind: ErrorKind, args: List<String>? = null, range: Range? = null) =
    shouldBeInvalidWithError(Parser::parseExpression, kind, args, range)
private fun Code.shouldBeInvalidStatementWithError(kind: ErrorKind, args: List<String>? = null, range: Range? = null) =
    shouldBeInvalidWithError(
        Parser::parseModuleItem,
        kind,
        args,
        range,
    )
private fun Code.shouldBeInvalidProgramWithError(kind: ErrorKind, args: List<String>? = null, range: Range? = null) =
    shouldBeInvalidWithError(Parser::parseModule, kind, args, range)
private inline fun <N: Node> Parser.parseSuccessfully(parseFn: Parser.() -> N?) =
    parseFn().let { node ->
        assert(!hasError) {
            """
                Error occurred: $error
                Stack trace:
            """.trimIndent() + "\n${stackTrace?.joinToString("\n") { "    $it" }}"
        }
        assertNotNull(node)
        node
    }
private inline fun Parser.parseUnsuccessfully(parseFn: Parser.() -> Node?) =
    run {
        assertNull(parseFn())
        assert(hasError)
        error!!
    }
private fun createParser(code: Code) =
    code.trimIndent()
        .let(::Tokenizer)
        .let(::Parser)

private typealias Code = String
