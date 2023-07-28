package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.ErrorKind
import io.github.andjsrk.v4.error.SyntaxErrorKind
import io.github.andjsrk.v4.parse.node.*
import org.junit.jupiter.api.Test
import kotlin.test.*

internal class ParserTest {
    @Test
    fun testTokenizerErrorEjection() {
        """
            "\x"
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.INVALID_HEX_ESCAPE_SEQUENCE)
    }
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
            elements[0].assertTypeAnd<PropertyNode> {
                key.assertIdentifierNamed("a")
                assertIs<NumberLiteralNode>(value)
            }
            elements[1].assertTypeAnd<PropertyNode> {
                key.assertIdentifierNamed("b")
            }
            elements[2].assertTypeAnd<SpreadNode> {
                expression.assertIdentifierNamed("c")
            }
        }

        """
            { a }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeAnd<PropertyNode> {
                key.assertIdentifierNamed("a")
            }
        }

        """
            { [0] }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeAnd<PropertyNode> {
                assertIs<ComputedPropertyKeyNode>(key)
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
            elements[0].assertTypeAnd<ObjectMethodNode> {
                name.assertIdentifierNamed("a")
                assert(parameters.elements.isEmpty())
                assert(!isAsync && !isGenerator)
            }
        }

        """
            { async a() {} }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeAnd<ObjectMethodNode> {
                name.assertIdentifierNamed("a")
                assert(isAsync)
            }
        }

        """
            { gen a() {} }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeAnd<ObjectMethodNode> {
                name.assertIdentifierNamed("a")
                assert(isGenerator)
            }
        }

        """
            { async gen a() {} }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeAnd<ObjectMethodNode> {
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
            elements[0].assertTypeAnd<ObjectMethodNode> {
                name.assertIdentifierNamed("async")
                assert(!isAsync)
            }
        }

        """
            { get a() {} }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeAnd<ObjectGetterNode> {
                name.assertIdentifierNamed("a")
            }
        }

        """
            { get a(x) {} }
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.BAD_GETTER_ARITY)

        """
            { set a(x) {} }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeAnd<ObjectSetterNode> {
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
            elements[0].assertTypeAnd<ObjectMethodNode> {
                name.assertIdentifierNamed("get")
                assert(isAsync)
            }
        }
    }
    @Test
    fun testDistinguishBetweenObjectLiteralAndBlock() {
        """
            {}
        """.shouldBeValidModuleAnd {
            assertIs<BlockNode>(elements[0])
        }

        """
            ({})
        """.shouldBeValidModuleAnd {
            val obj = elements[0]
                .unwrapExprStmt<ParenthesizedExpressionNode>()
                .expression
            assertIs<ObjectLiteralNode>(obj)
        }
    }
    @Test
    fun testTemplateLiteral() {
        """
            `123`
        """.shouldBeValidExpressionAnd<TemplateLiteralNode> {
            assert(strings.single().value == "123")
            assert(expressions.isEmpty())
        }

        """
            `123${"\${0}"}456`
        """.shouldBeValidExpressionAnd<TemplateLiteralNode> {
            assert(strings[0].value == "123")
            assertIs<NumberLiteralNode>(expressions.single())
            assert(strings[1].value == "456")
        }

        """
            `${"\${0}\${0}"}`
        """.shouldBeValidExpressionAnd<TemplateLiteralNode> {
            assert(strings.size == 3)
            assert(expressions.size == 2)
        }

        """
            `123${"\${{}}"}`
        """.shouldBeValidExpressionAnd<TemplateLiteralNode> {
            assert(strings[0].value == "123")
            assertIs<ObjectLiteralNode>(expressions.single())
            assert(strings[1].value.isEmpty())
        }

        """
            `a
            b`
        """.shouldBeValidExpressionAnd<TemplateLiteralNode> {
            // note that no space between `a` and `b` is because of `.trimIndent()` call in test helper function, not an actual behavior
            assert(strings[0].value == "a\nb")
        }

        """
            `123${"\${"}
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.UNEXPECTED_EOS)
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
        """.shouldBeInvalidModuleWithError(SyntaxErrorKind.SUPER_OPTIONAL_CHAIN)

        """
            import?.()
        """.shouldBeInvalidModuleWithError(SyntaxErrorKind.UNEXPECTED_TOKEN, listOf("?."))
    }
    @Test
    fun testNormalCallMixedWithMemberExpression() {
        """
            a().b
        """.shouldBeValidExpressionAnd<MemberExpressionNode> {
            `object`.assertTypeAnd<NormalCallNode> {
                callee.assertIdentifierNamed("a")
            }
            property.assertIdentifierNamed("b")
        }

        """
            a?.().b
        """.shouldBeValidExpressionAnd<MemberExpressionNode> {
            `object`.assertTypeAnd<NormalCallNode> {
                callee.assertIdentifierNamed("a")
                assert(isOptionalChain)
            }
            property.assertIdentifierNamed("b")
        }
    }
    @Test
    fun testTaggedTemplate() {
        """
            a``
        """.shouldBeValidExpressionAnd<TaggedTemplateNode> {
            callee.assertIdentifierNamed("a")
        }

        """
            a?.b.c``
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.TAGGED_TEMPLATE_OPTIONAL_CHAIN)

        // modified to
        // CallExpression [no LineTerminator here] TemplateLiteral
        """
            a
            ``
        """.shouldBeValidModuleAnd {
            elements[0].unwrapExprStmt<IdentifierNode>()
            elements[1].unwrapExprStmt<TemplateLiteralNode>()
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
            callee.assertTypeAnd<MemberExpressionNode> {
                `object`.assertIdentifierNamed("A")
                property.assertIdentifierNamed("B")
            }
            assert(arguments.elements.size == 2)
        }

        """
            new A().b
        """.shouldBeValidExpressionAnd<MemberExpressionNode> {
            `object`.assertTypeAnd<NewExpressionNode> {
                callee.assertIdentifierNamed("A")
            }
            property.assertIdentifierNamed("b")
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
        """.shouldBeValidModuleAnd {
            val exprs = elements.map { it.unwrapExprStmt<ExpressionNode>() }

            exprs[0].assertIdentifierNamed("a")
            exprs[1].assertTypeAnd<UpdateNode> {
                assert(isPrefixed)
            }
        }

        """
            a
            ++b
        """.shouldBeValidModuleAnd {
            val exprs = elements.map { it.unwrapExprStmt<ExpressionNode>() }

            exprs[0].assertIdentifierNamed("a")
            exprs[1].assertTypeAnd<UpdateNode> {
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
        """.shouldBeInvalidModuleWithError(SyntaxErrorKind.UNEXPECTED_TOKEN_UNARY_EXPONENTIATION)
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

                block(elements[it].unwrapExprStmt(), isAsyncCase)
            }
        }

        """
            () => 0
            async () => 0
        """.shouldBeValidModuleAnd {
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
        """.shouldBeValidModuleAnd {
            forBothSyncAndAsync {
                body.assertTypeAnd<BlockNode> {
                    assert(elements.size == 1)
                }
            }
        }

        """
            x => 0
            async x => 0
        """.shouldBeValidModuleAnd {
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
        """.shouldBeInvalidModuleWithError(SyntaxErrorKind.ELEMENT_AFTER_REST)

        """
            ([a, b = 1]) => 0
            async ([a, b = 1]) => 0
        """.shouldBeValidModuleAnd {
            forBothSyncAndAsync {
                val params = parameters.elements
                assert(params.size == 1)
                params[0].unwrapNonRest<ArrayBindingPatternNode>().run {
                    assert(elements.size == 2)
                    elements[0].unwrapNonRest<IdentifierNode>()
                    elements[1].assertTypeAnd<NonRestNode> {
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
        """.shouldBeValidModuleAnd {
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
        """.shouldBeValidModuleAnd {
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
        """.shouldBeValidModuleAnd {
            forBothSyncAndAsync {
                parameters.elements[0].unwrapRest<ObjectBindingPatternNode>()
                    .elements[0].unwrapNonRest<IdentifierNode>()
            }
        }

        """
            ({ ab: a, b = 1, c, ...d }) => 0
            async ({ ab: a, b = 1, c, ...d }) => 0
        """.shouldBeValidModuleAnd {
            forBothSyncAndAsync {
                val params = parameters.elements
                assert(params.size == 1)
                params[0].unwrapNonRest<ObjectBindingPatternNode>().run {
                    assert(elements.size == 4)
                    elements[0].assertTypeAnd<NonRestObjectPropertyNode> {
                        key.assertIdentifierNamed("ab")
                        binding.assertIdentifierNamed("a")
                    }
                    elements[1].assertTypeAnd<NonRestNode> {
                        binding.assertIdentifierNamed("b")
                        assertIs<NumberLiteralNode>(default)
                    }
                    elements[2].assertTypeAnd<NonRestObjectPropertyNode> {
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
        """.shouldBeInvalidModuleWithError(SyntaxErrorKind.UNEXPECTED_TOKEN, listOf("=>"))

        """
            (a = await 0) => 0
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.AWAIT_EXPRESSION_FORMAL_PARAMETER)

        """
            (a = yield 0) => 0
        """.shouldBeInvalidModuleWithError(SyntaxErrorKind.YIELD_IN_PARAMETER)

        """
            ({ ...{} }) => 0
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.INVALID_REST_BINDING_PATTERN)

        """
            gen () => 0
            async gen () => 0
        """.shouldBeValidModuleAnd {
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
            assertTypeAnd<BinaryExpressionNode> {
                assert(operation == BinaryOperationType.ASSIGN)
            }
        }
    }
    // </editor-fold>
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
            assertIs<NormalLexicalDeclarationNode>(init)
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
            continue
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.ILLEGAL_CONTINUE)

        """
            for (;;) break
        """.shouldBeValidStatementAnd<NormalForNode> {
            assertIs<BreakNode>(body)
        }

        """
            break
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.ILLEGAL_BREAK)
    }
    @Test
    fun testReturn() {
        """
            () => {
                return 0
            }
        """.shouldBeValidStatementAnd<ExpressionStatementNode> {
            unwrapExprStmt<ArrowFunctionNode>().run {
                body.assertTypeAnd<BlockNode> {
                    elements[0].assertTypeAnd<ReturnNode> {
                        assertIs<NumberLiteralNode>(expression)
                    }
                }
            }
        }

        """
            () => {
                return
                0
            }
        """.shouldBeValidStatementAnd<ExpressionStatementNode> {
            unwrapExprStmt<ArrowFunctionNode>().run {
                body.assertTypeAnd<BlockNode> {
                    elements[0].assertTypeAnd<ReturnNode> {
                        assertNull(expression)
                    }
                    elements[1].assertTypeAnd<ExpressionStatementNode> {
                        assertIs<NumberLiteralNode>(expression)
                    }
                }
            }
        }

        """
            return
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.ILLEGAL_RETURN)
    }
    @Test
    fun testThrow() {
        """
            throw 0
        """.shouldBeValidStatementAnd<ThrowNode> {
            assertIs<NumberLiteralNode>(expression)
        }
        """
            throw
            0
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.NEWLINE_AFTER_THROW)
    }
    @Test
    fun testTry() {
        """
            try {} catch (e) {}
        """.shouldBeValidStatementAnd<TryNode> {
            val catch = catch
            assertNotNull(catch)
            assertNotNull(catch.binding)
            assertNull(finallyBody)
        }
        """
            try {} catch {}
        """.shouldBeValidStatementAnd<TryNode> {
            val catch = catch
            assertNotNull(catch)
            assertNull(catch.binding)
            assertNull(finallyBody)
        }

        """
            try {} finally {}
        """.shouldBeValidStatementAnd<TryNode> {
            assertNull(catch)
            assertNotNull(finallyBody)
        }

        """
            try {} catch {} finally {}
        """.shouldBeValidStatementAnd<TryNode> {
            assertNotNull(catch)
            assertNotNull(finallyBody)
        }

        """
            try {}
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.NO_CATCH_OR_FINALLY)
    }
    @Test
    fun testLexicalDeclaration() {
        """
            var a = 1
        """.shouldBeValidStatementAnd<NormalLexicalDeclarationNode> {
            assert(kind == LexicalDeclarationKind.VAR)
            bindings[0].run {
                binding.assertIdentifierNamed("a")
                assertIs<NumberLiteralNode>(value)
            }
        }

        """
            let a = 1
        """.shouldBeValidStatementAnd<NormalLexicalDeclarationNode> {
            assert(kind == LexicalDeclarationKind.LET)
        }

        """
            var a
        """.shouldBeValidStatementAnd<NormalLexicalDeclarationNode> {
            assertNull(bindings[0].value)
        }

        """
            let a
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.DECLARATION_MISSING_INITIALIZER)

        """
            var {}
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.DECLARATION_MISSING_INITIALIZER)

        """
            let a = 1, b = 2
        """.shouldBeValidStatementAnd<NormalLexicalDeclarationNode> {
            bindings[0].run {
                binding.assertIdentifierNamed("a")
                assertIs<NumberLiteralNode>(value)
            }
            bindings[1].run {
                binding.assertIdentifierNamed("b")
                assertIs<NumberLiteralNode>(value)
            }
        }
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
            elements[0].assertTypeAnd<FieldNode> {
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
            elements[0].assertTypeAnd<FieldNode> {
                assertNull(value)
            }
        }

        """
            class A {
                static a = 0
            }
        """.shouldBeValidStatementAnd<ClassDeclarationNode> {
            elements[0].assertTypeAnd<FieldNode> {
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
            elements[0].assertTypeAnd<FieldNode> {
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
            class A {
                constructor() {}
                constructor() {}
            }
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.DUPLICATE_CONSTRUCTOR)

        """
            class A {
                a() {}
                a = 1
            }
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.DUPLICATE_CLASS_ELEMENT_NAMES)

        """
            class A {
                get a() {}
                set a(x) {}
            }
        """.shouldBeValidStatementAnd<ClassDeclarationNode> {
            assertIs<ClassGetterNode>(elements[0])
            assertIs<ClassSetterNode>(elements[1])
        }

        """
            class A {
                get a() {}
                get a() {}
            }
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.DUPLICATE_CLASS_ELEMENT_NAMES)

        """
            class A {
                a() {}
                "a"() {}
            }
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.DUPLICATE_CLASS_ELEMENT_NAMES)
    }
    @Test
    fun testSuper() {
        """
            class A extends P {
                constructor() {
                    super()
                    super.a
                }
            }
        """.shouldBeValidStatementAnd<ClassDeclarationNode> {
            constructor!!.body.elements.run {
                assertIs<SuperCallNode>(this[0].unwrapExprStmt())
                assertIs<SuperPropertyNode>(this[1].unwrapExprStmt())
            }
        }

        """
            class A {
                constructor() {
                    super()
                }
            }
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.UNEXPECTED_SUPER)

        """
            class A {
                constructor() {
                    super.a
                }
            }
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.UNEXPECTED_SUPER)

        """
            class A extends P {
                a() {
                    super()
                }
            }
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.UNEXPECTED_SUPER)

        """
            class A extends P {
                a() {
                    super.a
                }
            }
        """.shouldBeValidStatementAnd<ClassDeclarationNode> {}

        """
            class A extends P {
                a = super()
            }
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.UNEXPECTED_SUPER)

        """
            {
                a() {
                    super.a
                }
            }
        """.shouldBeValidExpressionAnd<ObjectLiteralNode> {
            elements[0].assertTypeAnd<ObjectMethodNode> {
                assertIs<SuperPropertyNode>(body.elements[0].unwrapExprStmt())
            }
        }

        """
            {
                a() {
                    super()
                }
            }
        """.shouldBeInvalidExpressionWithError(SyntaxErrorKind.UNEXPECTED_SUPER)
    }
    @Test
    fun testImportDeclaration() {
        """
            import "mod" with { a, b as c }
        """.shouldBeValidStatementAnd<NamedImportDeclarationNode> {
            specifiers[0].run {
                name.assertIdentifierNamed("a")
                assert(name.value == alias.value)
            }
            specifiers[1].run {
                name.assertIdentifierNamed("b")
                alias.assertIdentifierNamed("c")
            }
        }

        """
            import "mod" with { if }
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.UNEXPECTED_RESERVED)

        """
            import "mod" as a
        """.shouldBeValidStatementAnd<NamespaceImportDeclarationNode> {}

        """
            import "mod"
        """.shouldBeValidStatementAnd<EffectImportDeclarationNode> {}
    }
    @Test
    fun testExportDeclaration() {
        """
            export let a = 0
        """.shouldBeValidStatementAnd<NamedSingleExportDeclarationNode> {
            assertIs<NormalLexicalDeclarationNode>(declaration)
        }

        """
            export { a, b as c }
        """.shouldBeValidStatementAnd<NamedExportDeclarationNode> {
            specifiers[0].run {
                name.assertIdentifierNamed("a")
                assert(name.value == alias.value)
            }
            specifiers[1].run {
                name.assertIdentifierNamed("b")
                alias.assertIdentifierNamed("c")
            }
        }

        """
            export "mod" with { a, b as c }
        """.shouldBeValidStatementAnd<NamedReExportDeclarationNode> {
            specifiers[0].run {
                name.assertIdentifierNamed("a")
                assert(name.value == alias.value)
            }
            specifiers[1].run {
                name.assertIdentifierNamed("b")
                alias.assertIdentifierNamed("c")
            }
        }

        """
            export { if }
        """.shouldBeInvalidStatementWithError(SyntaxErrorKind.UNEXPECTED_RESERVED)

        """
            export "mod" with { if }
        """.shouldBeValidStatementAnd<NamedReExportDeclarationNode> {
            specifiers[0].name.assertIdentifierNamed("if")
        }

        """
            export "mod" *
        """.shouldBeValidStatementAnd<AllReExportDeclarationNode> {}
    }
    @Test
    fun testModuleEarlyErrors() {
        """
            import "mod" with { a, a }
        """.shouldBeInvalidModuleWithError(SyntaxErrorKind.VAR_REDECLARATION)

        """
            export { a, a }
        """.shouldBeInvalidModuleWithError(SyntaxErrorKind.DUPLICATE_EXPORT)

        """
            export { a, b as a }
        """.shouldBeInvalidModuleWithError(SyntaxErrorKind.DUPLICATE_EXPORT)

        """
            export var a
            export { a }
        """.shouldBeInvalidModuleWithError(SyntaxErrorKind.DUPLICATE_EXPORT)

        """
            super.a
        """.shouldBeInvalidModuleWithError(SyntaxErrorKind.UNEXPECTED_SUPER)
    }
}

private inline fun <reified T: BindingElementNode> MaybeRestNode.unwrapNonRest() =
    this.assertType<NonRestNode>()
        .binding.assertType<T>()
private inline fun <reified T: BindingElementNode> MaybeRestNode.unwrapRest() =
    this.assertType<RestNode>()
        .binding.assertType<T>()
private inline fun <reified Expr: ExpressionNode> StatementNode.unwrapExprStmt() =
    this.assertType<ExpressionStatementNode>()
        .expression.assertType<Expr>()
private fun Node.assertIdentifierNamed(name: String) {
    assertTypeAnd<IdentifierNode> {
        assert(value == name)
    }
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
    shouldBeValidAnd(Parser::parseModuleItem, block)
private inline fun Code.shouldBeValidModuleAnd(block: ModuleNode.() -> Unit) =
    shouldBeValidAnd(Parser::parseModule, block)
private fun Code.shouldBeInvalidWithError(parseFn: Parser.() -> Node?, kind: ErrorKind, args: List<String>? = null, range: Range? = null) {
    val parser = createParser(this)
    val error = parser.parseUnsuccessfully(parseFn)
    assert(error.kind == kind) {
        """
            Expected: $kind
            Actual: $error
        """.trimIndent() + parser.stackTrace?.toErrorMessagePart()
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
    shouldBeInvalidWithError(Parser::parseModuleItem, kind, args, range)
private fun Code.shouldBeInvalidModuleWithError(kind: ErrorKind, args: List<String>? = null, range: Range? = null) =
    shouldBeInvalidWithError(Parser::parseModule, kind, args, range)
private inline fun <N: Node> Parser.parseSuccessfully(parseFn: Parser.() -> N?) =
    parseFn().let { node ->
        assert(!hasError) { createErrorMsg() }
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
    Parser(code.trimIndent())

private typealias Code = String
