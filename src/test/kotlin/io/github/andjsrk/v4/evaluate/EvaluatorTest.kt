package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.createErrorMsg
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.Binding
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.parse.Parser
import org.junit.jupiter.api.Test
import kotlin.test.*

internal class EvaluatorTest {
    @Test
    fun testPrimitiveLiteral() {
        evaluationOf("""
            "abc"
        """).shouldBeNormalAnd<StringType> {
            assert(value == "abc")
        }

        evaluationOf("""
            123
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 123.0)
        }

        evaluationOf("""
            true
        """).shouldBeNormalAnd<BooleanType> {
            assertTrue(value)
        }

        evaluationOf("""
            null
        """).shouldBeNormalAnd<NullType> {}

        evaluationOf("""
            123n
        """).shouldBeNormalAnd<BigIntType> {
            assert(value == 123.toBigInteger())
        }
    }
    @Test
    fun testUnaryExpression() {
        evaluationOf("""
            void 0
        """).shouldBeNormalAnd<NullType> {}

        evaluationOf("""
            typeof "abc"
        """).shouldBeNormalAnd<StringType> {
            assert(value == "string")
        }

        evaluationOf("""
            typeof 123
        """).shouldBeNormalAnd<StringType> {
            assert(value == "number")
        }

        evaluationOf("""
            typeof true
        """).shouldBeNormalAnd<StringType> {
            assert(value == "boolean")
        }

        evaluationOf("""
            typeof null
        """).shouldBeNormalAnd<StringType> {
            assert(value == "null")
        }

        evaluationOf("""
            -123
        """).shouldBeNormalAnd<NumberType> {
            assert(value == -123.0)
        }

        evaluationOf("""
            -true
        """).shouldBeThrowAnd {}

        evaluationOf("""
            ~123
        """).shouldBeNormalAnd<NumberType> {
            assert(value == -124.0)
        }

        evaluationOf("""
            ~true
        """).shouldBeThrowAnd {}

        evaluationOf("""
            !true
        """).shouldBeNormalAnd<BooleanType> {
            assertFalse(value)
        }

        evaluationOf("""
            !0
        """).shouldBeThrowAnd {}
    }
    @Test
    fun testArithmeticOperator() {
        evaluationOf("""
            2 ** 4
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 16.0)
        }

        evaluationOf("""
            3 ** 0
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 1.0)
        }

        evaluationOf("""
            "a" ** 1
        """).shouldBeThrowAnd {}

        evaluationOf("""
            1 ** 1n
        """).shouldBeThrowAnd {}

        evaluationOf("""
            2 * 3
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 6.0)
        }

        evaluationOf("""
            "2" * 3
        """).shouldBeThrowAnd {}

        evaluationOf("""
            4 / 2
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 2.0)
        }

        evaluationOf("""
            5 % 2
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 1.0)
        }

        evaluationOf("""
            2.5 % 1
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 0.5)
        }

        evaluationOf("""
            -3 % 2
        """).shouldBeNormalAnd<NumberType> {
            assert(value == -1.0)
        }

        evaluationOf("""
            1 + 2
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 3.0)
        }

        evaluationOf("""
            "1" + "2"
        """).shouldBeNormalAnd<StringType> {
            assert(value == "12")
        }

        evaluationOf("""
            "1" + 2
        """).shouldBeNormalAnd<StringType> {
            assert(value == "12")
        }

        evaluationOf("""
            2 - 1
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 1.0)
        }

        evaluationOf("""
            "2" - 1
        """).shouldBeThrowAnd {}
    }
    @Test
    fun testBitwiseOperator() {
        evaluationOf("""
            1 << 4
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 16.0)
        }

        evaluationOf("""
            16 >> 4
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 1.0)
        }

        evaluationOf("""
            -2 >> 1
        """).shouldBeNormalAnd<NumberType> {
            assert(value == -1.0)
        }

        evaluationOf("""
            16 >>> 4
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 1.0)
        }

        evaluationOf("""
            -2 >>> 1
        """).shouldBeNormalAnd<NumberType> {
            assert(value == Int.MAX_VALUE.toDouble())
        }

        evaluationOf("""
            7 & 11
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 3.0)
        }

        evaluationOf("""
            3 | 10
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 11.0)
        }

        evaluationOf("""
            1.1 | 0
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 1.0)
        }
    }
    @Test
    fun testRelationalOperator() {
        evaluationOf("""
            1 < 2
        """).shouldBeNormalAnd<BooleanType> {
            assertTrue(value)
        }

        evaluationOf("""
            1 < 1
        """).shouldBeNormalAnd<BooleanType> {
            assertFalse(value)
        }

        evaluationOf("""
            2 < 1
        """).shouldBeNormalAnd<BooleanType> {
            assertFalse(value)
        }

        evaluationOf("""
            1 <= 2
        """).shouldBeNormalAnd<BooleanType> {
            assertTrue(value)
        }

        evaluationOf("""
            1 <= 1
        """).shouldBeNormalAnd<BooleanType> {
            assertTrue(value)
        }

        evaluationOf("""
            2 <= 1
        """).shouldBeNormalAnd<BooleanType> {
            assertFalse(value)
        }

        evaluationOf("""
            2 > 1
        """).shouldBeNormalAnd<BooleanType> {
            assertTrue(value)
        }

        evaluationOf("""
            1 > 1
        """).shouldBeNormalAnd<BooleanType> {
            assertFalse(value)
        }

        evaluationOf("""
            1 > 2
        """).shouldBeNormalAnd<BooleanType> {
            assertFalse(value)
        }

        evaluationOf("""
            2 >= 1
        """).shouldBeNormalAnd<BooleanType> {
            assertTrue(value)
        }

        evaluationOf("""
            1 >= 1
        """).shouldBeNormalAnd<BooleanType> {
            assertTrue(value)
        }

        evaluationOf("""
            1 >= 2
        """).shouldBeNormalAnd<BooleanType> {
            assertFalse(value)
        }
    }
    @Test
    fun testEqualOperator() {
        arrayOf("==" to true, "!==" to false).forEach { (op, expected) ->
            fun Completion.shouldEqual() =
                this.shouldBeNormalAnd<BooleanType> {
                    assert(value == expected)
                }
            fun Completion.shouldNotEqual() =
                this.shouldBeNormalAnd<BooleanType> {
                    assert(value != expected)
                }

            evaluationOf("""
                1 $op 1
            """).shouldEqual()

            evaluationOf("""
                1 $op 2
            """).shouldNotEqual()

            evaluationOf("""
                -0 $op 0
            """).shouldEqual()

            evaluationOf("""
                (0 / 0) $op (0 / 0)
            """).shouldNotEqual()

            evaluationOf("""
                1 $op "a"
            """).shouldNotEqual()

            evaluationOf("""
                "abc" $op "abc"
            """).shouldEqual()

            evaluationOf("""
                "abc" $op "abd"
            """).shouldNotEqual()

            evaluationOf("""
                true $op true
            """).shouldEqual()

            evaluationOf("""
                true $op false
            """).shouldNotEqual()
        }
    }
    @Test
    fun testLogicalOperator() {
        evaluationOf("""
            true && true
        """).shouldBeNormalAnd<BooleanType> {
            assertTrue(value)
        }

        evaluationOf("""
            false && true
        """).shouldBeNormalAnd<BooleanType> {
            assertFalse(value)
        }

        evaluationOf("""
            true && 0
        """).shouldBeNormalAnd<NumberType> {}

        evaluationOf("""
            true || false
        """).shouldBeNormalAnd<BooleanType> {
            assertTrue(value)
        }

        evaluationOf("""
            false || true
        """).shouldBeNormalAnd<BooleanType> {
            assertTrue(value)
        }

        evaluationOf("""
            false || false
        """).shouldBeNormalAnd<BooleanType> {
            assertFalse(value)
        }

        evaluationOf("""
            true || 0
        """).shouldBeNormalAnd<BooleanType> {
            // 0 will not be evaluated, so it is just `true`
            assertTrue(value)
        }

        evaluationOf("""
            false || 0
        """).shouldBeThrowAnd {}

        evaluationOf("""
            null ?? 0
        """).shouldBeNormalAnd<NumberType> {}

        evaluationOf("""
            true ?? false
        """).shouldBeNormalAnd<BooleanType> {
            assertTrue(value)
        }
    }
    @Test
    fun testIfExpression() {
        evaluationOf("""
            (if (true) 1 else 0)
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 1.0)
        }

        evaluationOf("""
            (if (false) 1 else 0)
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 0.0)
        }

        evaluationOf("""
            (if (1) 1 else 0)
        """).shouldBeThrowAnd {}
    }
    @Test
    fun testLexicalDeclaration() {
        evaluationOf("""
            let a = 0
        """).shouldBeNormalAnd<NullType> {
            variableNamed("a").run {
                assertFalse(isMutable)
                assertIs<NumberType>(value)
            }
        }

        evaluationOf("""
            var a
        """).shouldBeNormalAnd<NullType> {
            variableNamed("a").run {
                assertTrue(isMutable)
                assertIs<NullType>(value)
            }
        }
    }
    @Test
    fun testAssignment() {
        evaluationOf("""
            var a = 0
            a = 1
        """).shouldBeNormalAnd<NumberType> {
            variableNamed("a").shouldBeTypedAs<NumberType> {
                assert(value == 1.0)
            }
        }

        evaluationOf("""
            var a = 0
            a += 1
        """).shouldBeNormalAnd<NumberType> {
            variableNamed("a").shouldBeTypedAs<NumberType> {
                assert(value == 1.0)
            }
        }
    }
    @Test
    fun testIfStatement() {
        evaluationOf("""
            if (true) 0
            else 1
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 0.0)
        }

        evaluationOf("""
            if (true) 0
        """).shouldBeNormalAnd<NumberType> {}

        evaluationOf("""
            if (false) 0
        """).shouldBeNormalAnd<NullType> {}
    }
    @Test
    fun testWhile() {
        evaluationOf("""
            var run = true
            var count = 0
            while (run) {
                count += 1
                run = false
            }
        """).shouldBeNormalAnd {
            variableNamed("count").shouldBeTypedAs<NumberType> {
                assert(value == 1.0)
            }
        }

        evaluationOf("""
            while (false) 0
        """).shouldBeNormalAnd<NullType> {}
    }
    @Test
    fun testIterationFlowControlStatement() {
        evaluationOf("""
            var i = 0
            var a = 0
            while (i < 3) {
                i += 1
                if (i == 2) continue
                a += 1
            }
        """).shouldBeNormalAnd {
            variableNamed("a").shouldBeTypedAs<NumberType> {
                assert(value == 2.0)
            }
        }

        evaluationOf("""
            while (true) {
                break
                0
            }
        """).shouldBeNormalAnd<NullType> {}
    }
    @Test
    fun testNormalFor() {
        evaluationOf("""
            var a = 0
            for (var i = 0; i < 5; i += 1) a += i
        """).shouldBeNormalAnd<NumberType> {
            variableNamed("a").shouldBeTypedAs<NumberType> {
                assert(value == (0..4).sum().toDouble())
            }
        }

        evaluationOf("""
            var i = 0
            for (; i < 5; i += 1);
        """).shouldBeNormalAnd<NullType> {
            variableNamed("i").shouldBeTypedAs<NumberType> {
                assert(value == 5.0)
            }
        }

        evaluationOf("""
            var i = 0
            for (; i < 5;) i += 1
        """).shouldBeNormalAnd {
            variableNamed("i").shouldBeTypedAs<NumberType> {
                assert(value == 5.0)
            }
        }

        evaluationOf("""
            var i = 0
            for (;;) {
                i += 1
                if (i == 5) break
            }
        """).shouldBeNormalAnd {
            variableNamed("i").shouldBeTypedAs<NumberType> {
                assert(value == 5.0)
            }
        }
    }
    @Test
    fun testThrow() {
        evaluationOf("""
            throw 0
        """).shouldBeThrowAnd {}

        evaluationOf("""
            while (true) throw 0
        """).shouldBeThrowAnd {}
    }
    @Test
    fun testTry() {
        evaluationOf("""
            var threw = false
            try {
                throw 0
            } catch {
                threw = true
            }
        """).shouldBeNormalAnd {
            variableNamed("threw").shouldBeTypedAs<BooleanType> {
                assertTrue(value)
            }
        }

        evaluationOf("""
            var threw = false
            try {
                throw 0
            } finally {
                threw = true
            }
        """).shouldBeThrowAnd {
            variableNamed("threw").shouldBeTypedAs<BooleanType> {
                assertTrue(value)
            }
        }

        evaluationOf("""
            try {
                throw 0
            } catch {
                0
            } finally {
                1
            }
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 0.0)
        }

        evaluationOf("""
            try {
                throw 0
            } finally {
                throw 1
            }
        """).shouldBeThrowAnd<NumberType> {
            assert(value == 1.0)
        }
    }
}

private fun variableNamed(name: String): Binding {
    val binding = Evaluator.runningExecutionContext.lexicalEnvironment.bindings[name]
    assertNotNull(binding)
    return binding
}
private inline fun <reified Value: LanguageType> Binding.shouldBeTypedAs(block: Value.() -> Unit) {
    val value = value
    assertIs<Value>(value)
    block(value)
}
private fun Completion.shouldBeNormalAnd(block: () -> Unit) {
    assert(this.isNormal)
    block()
    Evaluator.cleanup()
}
private inline fun <reified Value: LanguageType> Completion.shouldBeNormalAnd(crossinline block: Value.() -> Unit) =
    this.shouldBeNormalAnd {
        val value = value
        assertIs<Value>(value)
        block(value)
    }
private fun Completion.shouldBeThrowAnd(block: () -> Unit) {
    assert(type == Completion.Type.THROW)
    // TODO: put more assertions
}
private inline fun <reified Value: LanguageType> Completion.shouldBeThrowAnd(crossinline block: Value.() -> Unit) =
    this.shouldBeThrowAnd {
        val value = value
        assertIs<Value>(value)
        block(value)
    }
private fun evaluationOf(code: String): Completion {
    val parser = Parser(code.trimIndent())
    return parser.parseModule()
        ?.let { Evaluator.evaluate(it) }
        ?: throw AssertionError(parser.createErrorMsg())
}
