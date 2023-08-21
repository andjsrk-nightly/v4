package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
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
            fun EvaluationResult.shouldEqual() =
                this.shouldBeNormalAnd<BooleanType> {
                    assert(value == expected)
                }
            fun EvaluationResult.shouldNotEqual() =
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

            evaluationOf("""
                ({} $op {})
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
        """).shouldBeThrowAnd {}

        evaluationOf("""
            true &&> 0
        """).shouldBeNormalAnd<NumberType> {}

        evaluationOf("""
            false &&> 0
        """).shouldBeNormalAnd<BooleanType> {
            assertFalse(value)
        }

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
            var a
        """).shouldBeNormalAnd<NullType> {}

        evaluationOf("""
            var a
        """).shouldBeNormalAnd {
            variableNamed("a").run {
                assertTrue(isMutable)
                assertIs<NullType>(value)
            }
        }

        evaluationOf("""
            let a = 0
        """).shouldBeNormalAnd {
            variableNamed("a").run {
                assertFalse(isMutable)
                assertIs<NumberType>(value)
            }
        }
    }
    @Test
    fun testAssignment() {
        evaluationOf("""
            var a = 0
            a = 1
        """).shouldBeNormalAnd<NumberType>(moduleBlock={
            variableNamed("a").shouldBeTypedAs<NumberType> {
                assert(value == 1.0)
            }
        })

        evaluationOf("""
            var a = 0
            a += 1
        """).shouldBeNormalAnd<NumberType>(moduleBlock={
            variableNamed("a").shouldBeTypedAs<NumberType> {
                assert(value == 1.0)
            }
        })
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
        """).shouldBeNormalAnd {
            variableNamed("a").shouldBeTypedAs<NumberType> {
                assert(value == (0..4).sum().toDouble())
            }
        }

        evaluationOf("""
            var i = 0
            for (; i < 5; i += 1);
        """).shouldBeNormalAnd {
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
    @Test
    fun testObjectBasic() {
        evaluationOf("""
            ({ a: 0 })
        """).shouldBeNormalAnd<ObjectType> {
            dataPropertyNamed("a").run {
                value.assertTypeAnd<NumberType> {
                    assert(value == 0.0)
                }
            }
        }

        evaluationOf("""
            let a = 0
            let b = { a }
        """).shouldBeNormalAnd {
            variableNamed("b").shouldBeTypedAs<ObjectType> {
                dataPropertyNamed("a").run {
                    value.assertTypeAnd<NumberType> {
                        assert(value == 0.0)
                    }
                }
            }
        }

        evaluationOf("""
            ({ true })
        """).shouldBeNormalAnd<ObjectType> {
            dataPropertyNamed("true").run {
                value.assertTypeAnd<BooleanType> {
                    assertTrue(value)
                }
            }
        }

        evaluationOf("""
            ({ ["a"] })
        """).shouldBeNormalAnd<ObjectType> {
            dataPropertyNamed("a").run {
                value.assertTypeAnd<StringType> {
                    assert(value == "a")
                }
            }
        }

        evaluationOf("""
            var evaluationCount = 0
            ;({ [evaluationCount += 1] })
        """).shouldBeNormalAnd {
            variableNamed("evaluationCount").shouldBeTypedAs<NumberType> {
                assert(value == 1.0)
            }
        }
    }
    @Test
    fun testMember() {
        evaluationOf("""
            ({ a: 0 }).a
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 0.0)
        }

        evaluationOf("""
            ({ a: { b: 0 } })?.a.b
        """).shouldBeNormalAnd<NumberType> {
            assert(value == 0.0)
        }

        evaluationOf("""
            null?.a
        """).shouldBeNormalAnd<NullType> {}

        evaluationOf("""
            null?.a.b
        """).shouldBeNormalAnd<NullType> {}
    }
    @Test
    fun testArrowFunction() {
        evaluationOf("""
            let getZero = () => 0
            let zero = getZero()
        """).shouldBeNormalAnd {
            variableNamed("zero").shouldBeTypedAs<NumberType> {
                assert(value == 0.0)
            }
        }

        evaluationOf("""
            let addOne = (x) => x + 1
            let two = addOne(1)
        """).shouldBeNormalAnd {
            variableNamed("two").shouldBeTypedAs<NumberType> {
                assert(value == 2.0)
            }
        }

        evaluationOf("""
            let argsAsArr = (...args) => args
            let args = argsAsArr(1, 2, 3)
        """).shouldBeNormalAnd {
            variableNamed("args").shouldBeTypedAs<ArrayType> {
                for (i in 1..3) assert(at(i - 1) == NumberType(i.toDouble()))
            }
        }
    }
}

private fun ArrayType.at(index: Int): LanguageType? {
    val desc = properties[index.toString().languageValue] ?: return null
    assertIs<DataProperty>(desc)
    return desc.value
}
private fun ObjectType.dataPropertyNamed(name: String) =
    properties[name.languageValue].assertType<DataProperty>()
private fun SourceTextModule.variableNamed(name: String): Binding {
    val binding = environment.bindings[name]
    assertNotNull(binding)
    return binding
}
private inline fun <reified Value: LanguageType> Binding.shouldBeTypedAs(block: Value.() -> Unit) {
    value.assertTypeAnd<Value>(block)
}
private fun EvaluationResult.shouldBeNormalAnd(block: SourceTextModule.() -> Unit) {
    assertIs<Completion.Normal<*>>(completion)
    block(module)
}
private inline fun <reified Value: LanguageType> EvaluationResult.shouldBeNormalAnd(
    crossinline moduleBlock: SourceTextModule.() -> Unit = {},
    crossinline valueBlock: Value.() -> Unit = {},
) =
    this.shouldBeNormalAnd {
        completion.value.assertTypeAnd<Value> {
            valueBlock(this)
            moduleBlock(module)
        }
    }
private fun EvaluationResult.shouldBeThrowAnd(block: SourceTextModule.() -> Unit) {
    assertIs<Completion.Throw>(completion)
    block(module)
    // TODO: put more assertions
}
@JvmName("shouldBeThrowTypedAnd")
private inline fun <reified Value: LanguageType> EvaluationResult.shouldBeThrowAnd(crossinline block: Value.() -> Unit) =
    this.shouldBeThrowAnd {
        completion.value.assertTypeAnd<Value>(block)
    }
private fun evaluationOf(code: String): EvaluationResult {
    initializeRealm()
    return when (val moduleOrError = parseModule(code.trimIndent(), runningExecutionContext.realm)) {
        is Valid -> {
            val module = moduleOrError.value
            module.initializeEnvironment()
            val res = module.executeModuleWithoutIgnoringValue()
            EvaluationResult(res, module)
        }
        is Invalid -> throw moduleOrError.value
    }
}
/**
 * @see SourceTextModule.executeModule
 */
private fun SourceTextModule.executeModuleWithoutIgnoringValue(): NormalOrAbrupt {
    executionContextStack.addTop(ExecutionContext(realm, environment))
    val res = node.evaluate()
    executionContextStack.removeTop()
    return res
}
private data class EvaluationResult(val completion: Completion<*>, val module: SourceTextModule)
