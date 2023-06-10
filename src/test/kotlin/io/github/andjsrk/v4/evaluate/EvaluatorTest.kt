package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.createErrorMsg
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.spec.Binding
import io.github.andjsrk.v4.evaluate.type.spec.Completion
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
        """).shouldBeThrow()

        evaluationOf("""
            ~123
        """).shouldBeNormalAnd<NumberType> {
            assert(value == -124.0)
        }

        evaluationOf("""
            ~true
        """).shouldBeThrow()

        evaluationOf("""
            !true
        """).shouldBeNormalAnd<BooleanType> {
            assertFalse(value)
        }

        evaluationOf("""
            !0
        """).shouldBeThrow()
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
        """).shouldBeThrow()

        evaluationOf("""
            1 ** 1n
        """).shouldBeThrow()

        evaluationOf("""
            2 * 3
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 6.0)
        }

        evaluationOf("""
            "2" * 3
        """).shouldBeThrow()

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
        """).shouldBeThrow()
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
            fun BooleanType.assertEqual() =
                assert(value == expected)
            fun BooleanType.assertNotEqual() =
                assert(value != expected)

            evaluationOf("""
                1 $op 1
            """).shouldBeNormalAnd<BooleanType> {
                assertEqual()
            }

            evaluationOf("""
                1 $op 2
            """).shouldBeNormalAnd<BooleanType> {
                assertNotEqual()
            }

            evaluationOf("""
                -0 $op 0
            """).shouldBeNormalAnd<BooleanType> {
                assertEqual()
            }

            evaluationOf("""
                (0 / 0) $op (0 / 0)
            """).shouldBeNormalAnd<BooleanType> {
                assertNotEqual()
            }

            evaluationOf("""
                1 $op "a"
            """).shouldBeNormalAnd<BooleanType> {
                assertNotEqual()
            }

            evaluationOf("""
                "abc" $op "abc"
            """).shouldBeNormalAnd<BooleanType> {
                assertEqual()
            }

            evaluationOf("""
                "abc" $op "abd"
            """).shouldBeNormalAnd<BooleanType> {
                assertNotEqual()
            }

            evaluationOf("""
                true $op true
            """).shouldBeNormalAnd<BooleanType> {
                assertEqual()
            }

            evaluationOf("""
                true $op false
            """).shouldBeNormalAnd<BooleanType> {
                assertNotEqual()
            }
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
        """).shouldBeThrow()

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
        """).shouldBeThrow()
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
            variableNamed("a").run {
                val value = value
                assertIs<NumberType>(value)
                assert(value.value == 1.0)
            }
        }
    }
}

private fun variableNamed(name: String): Binding {
    val binding = Evaluator.runningExecutionContext.lexicalEnvironment.bindings[name]
    assertNotNull(binding)
    return binding
}
private inline fun <reified Value: LanguageType> Completion.shouldBeNormalAnd(block: Value.() -> Unit) {
    assert(type.isNormal)
    val value = value
    assertIs<Value>(value)
    block(value)
    Evaluator.cleanup()
}
private fun Completion.shouldBeThrow() {
    assert(type == Completion.Type.THROW)
    // TODO: put more assertions
}
private fun evaluationOf(code: String): Completion {
    val parser = Parser(code.trimIndent())
    return parser.parseModule()
        ?.let { Evaluator.evaluate(it) }
        ?: throw AssertionError(parser.createErrorMsg())
}
