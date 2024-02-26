package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.type.*
import org.junit.jupiter.api.Test
import kotlin.test.*

internal class EvaluatorTest {
    @Test
    fun testPrimitiveLiteral() {
        evaluationOf("""
            "abc"
        """).assertNormalAnd<StringType> {
            assert(value.nativeValue == "abc")
        }

        evaluationOf("""
            123
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 123.0)
        }

        evaluationOf("""
            true
        """).assertNormalAnd<BooleanType> {
            assertTrue(value.nativeValue)
        }

        evaluationOf("""
            null
        """).assertNormalAnd<NullType> {}

        evaluationOf("""
            123n
        """).assertNormalAnd<BigIntType> {
            assert(value.nativeValue == 123.toBigInteger())
        }
    }
    @Test
    fun testUnaryExpression() {
        evaluationOf("""
            void 0
        """).assertNormalAnd<NullType> {}

        evaluationOf("""
            typeof "abc"
        """).assertNormalAnd<StringType> {
            assert(value.nativeValue == "string")
        }

        evaluationOf("""
            typeof 123
        """).assertNormalAnd<StringType> {
            assert(value.nativeValue == "number")
        }

        evaluationOf("""
            typeof true
        """).assertNormalAnd<StringType> {
            assert(value.nativeValue == "boolean")
        }

        evaluationOf("""
            typeof null
        """).assertNormalAnd<StringType> {
            assert(value.nativeValue == "null")
        }

        evaluationOf("""
            -123
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == -123.0)
        }

        evaluationOf("""
            -true
        """).assertThrowAnd {}

        evaluationOf("""
            ~123
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == -124.0)
        }

        evaluationOf("""
            ~true
        """).assertThrowAnd {}

        evaluationOf("""
            !true
        """).assertNormalAnd<BooleanType> {
            assertFalse(value.nativeValue)
        }

        evaluationOf("""
            !0
        """).assertThrowAnd {}
    }
    @Test
    fun testArithmeticOperator() {
        evaluationOf("""
            2 ** 4
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 16.0)
        }

        evaluationOf("""
            3 ** 0
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 1.0)
        }

        evaluationOf("""
            "a" ** 1
        """).assertThrowAnd {}

        evaluationOf("""
            1 ** 1n
        """).assertThrowAnd {}

        evaluationOf("""
            2 * 3
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 6.0)
        }

        evaluationOf("""
            "2" * 3
        """).assertThrowAnd {}

        evaluationOf("""
            4 / 2
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 2.0)
        }

        evaluationOf("""
            5 % 2
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 1.0)
        }

        evaluationOf("""
            2.5 % 1
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 0.5)
        }

        evaluationOf("""
            -3 % 2
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == -1.0)
        }

        evaluationOf("""
            1 + 2
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 3.0)
        }

        evaluationOf("""
            "1" + "2"
        """).assertNormalAnd<StringType> {
            assert(value.nativeValue == "12")
        }

        evaluationOf("""
            "1" + 2
        """).assertNormalAnd<StringType> {
            assert(value.nativeValue == "12")
        }

        evaluationOf("""
            2 - 1
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 1.0)
        }

        evaluationOf("""
            "2" - 1
        """).assertThrowAnd {}
    }
    @Test
    fun testBitwiseOperator() {
        evaluationOf("""
            1 << 4
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 16.0)
        }

        evaluationOf("""
            16 >> 4
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 1.0)
        }

        evaluationOf("""
            -2 >> 1
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == -1.0)
        }

        evaluationOf("""
            16 >>> 4
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 1.0)
        }

        evaluationOf("""
            -2 >>> 1
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == Int.MAX_VALUE.toDouble())
        }

        evaluationOf("""
            7 & 11
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 3.0)
        }

        evaluationOf("""
            3 | 10
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 11.0)
        }

        evaluationOf("""
            1.1 | 0
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 1.0)
        }
    }
    @Test
    fun testRelationalOperator() {
        evaluationOf("""
            1 < 2
        """).assertNormalAnd<BooleanType> {
            assertTrue(value.nativeValue)
        }

        evaluationOf("""
            1 < 1
        """).assertNormalAnd<BooleanType> {
            assertFalse(value.nativeValue)
        }

        evaluationOf("""
            2 < 1
        """).assertNormalAnd<BooleanType> {
            assertFalse(value.nativeValue)
        }

        evaluationOf("""
            1 <= 2
        """).assertNormalAnd<BooleanType> {
            assertTrue(value.nativeValue)
        }

        evaluationOf("""
            1 <= 1
        """).assertNormalAnd<BooleanType> {
            assertTrue(value.nativeValue)
        }

        evaluationOf("""
            2 <= 1
        """).assertNormalAnd<BooleanType> {
            assertFalse(value.nativeValue)
        }

        evaluationOf("""
            2 > 1
        """).assertNormalAnd<BooleanType> {
            assertTrue(value.nativeValue)
        }

        evaluationOf("""
            1 > 1
        """).assertNormalAnd<BooleanType> {
            assertFalse(value.nativeValue)
        }

        evaluationOf("""
            1 > 2
        """).assertNormalAnd<BooleanType> {
            assertFalse(value.nativeValue)
        }

        evaluationOf("""
            2 >= 1
        """).assertNormalAnd<BooleanType> {
            assertTrue(value.nativeValue)
        }

        evaluationOf("""
            1 >= 1
        """).assertNormalAnd<BooleanType> {
            assertTrue(value.nativeValue)
        }

        evaluationOf("""
            1 >= 2
        """).assertNormalAnd<BooleanType> {
            assertFalse(value.nativeValue)
        }
    }
    @Test
    fun testEqualOperator() {
        arrayOf("==" to true, "!==" to false).forEach { (op, expected) ->
            fun EvaluationResult.shouldEqual() =
                this.assertNormalAnd<BooleanType> {
                    assert(value.nativeValue == expected)
                }
            fun EvaluationResult.shouldNotEqual() =
                this.assertNormalAnd<BooleanType> {
                    assert(value.nativeValue != expected)
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
        """).assertNormalAnd<BooleanType> {
            assertTrue(value.nativeValue)
        }

        evaluationOf("""
            false && true
        """).assertNormalAnd<BooleanType> {
            assertFalse(value.nativeValue)
        }

        evaluationOf("""
            true && 0
        """).assertThrowAnd {}

        // evaluationOf("""
        //     true &&> 0
        // """).shouldBeNormalAnd<NumberType> {}
        //
        // evaluationOf("""
        //     false &&> 0
        // """).shouldBeNormalAnd<BooleanType> {
        //     assertFalse(value.value)
        // }

        evaluationOf("""
            true || false
        """).assertNormalAnd<BooleanType> {
            assertTrue(value.nativeValue)
        }

        evaluationOf("""
            false || true
        """).assertNormalAnd<BooleanType> {
            assertTrue(value.nativeValue)
        }

        evaluationOf("""
            false || false
        """).assertNormalAnd<BooleanType> {
            assertFalse(value.nativeValue)
        }

        evaluationOf("""
            true || 0
        """).assertNormalAnd<BooleanType> {
            // 0 will not be evaluated, so it is just `true` instead of an error
            assertTrue(value.nativeValue)
        }

        evaluationOf("""
            false || 0
        """).assertThrowAnd {}

        evaluationOf("""
            null ?? 0
        """).assertNormalAnd<NumberType> {}

        evaluationOf("""
            true ?? false
        """).assertNormalAnd<BooleanType> {
            assertTrue(value.nativeValue)
        }
    }
    @Test
    fun testIfExpression() {
        evaluationOf("""
            (if (true) 1 else 0)
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 1.0)
        }

        evaluationOf("""
            (if (false) 1 else 0)
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 0.0)
        }

        evaluationOf("""
            (if (1) 1 else 0)
        """).assertThrowAnd {}
    }
    @Test
    fun testLexicalDeclaration() {
        evaluationOf("""
            var a
        """).assertNormalAnd<NullType> {}

        evaluationOf("""
            var a
        """).assertNormalAnd {
            variableNamed("a").run {
                assertTrue(isMutable)
                assertIs<NullType>(value)
            }
        }

        evaluationOf("""
            let a = 0
        """).assertNormalAnd {
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
        """).assertNormalAnd<NumberType> {
            val a = module.variableNamed("a")
                .assertTypedAs<NumberType>()
            assert(a.nativeValue == 1.0)
        }

        evaluationOf("""
            var a = 0
            a += 1
        """).assertNormalAnd<NumberType> {
            val a = module.variableNamed("a")
                .assertTypedAs<NumberType>()
            assert(a.nativeValue == 1.0)
        }
    }
    @Test
    fun testIfStatement() {
        evaluationOf("""
            if (true) 0
            else 1
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 0.0)
        }

        evaluationOf("""
            if (true) 0
        """).assertNormalAnd<NumberType> {}

        evaluationOf("""
            if (false) 0
        """).assertNormalAnd<NullType> {}
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
        """).assertNormalAnd {
            val count = variableNamed("count")
                .assertTypedAs<NumberType>()
            assert(count.nativeValue == 1.0)
        }

        evaluationOf("""
            while (false) 0
        """).assertNormalAnd<NullType> {}
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
        """).assertNormalAnd {
            val a = variableNamed("a")
                .assertTypedAs<NumberType>()
            assert(a.nativeValue == 2.0)
        }

        evaluationOf("""
            while (true) {
                break
                0
            }
        """).assertNormalAnd<NullType> {}
    }
    @Test
    fun testNormalFor() {
        evaluationOf("""
            var sum = 0
            for (var i = 0; i < 5; i += 1) sum += i
        """).assertNormalAnd {
            val sum = variableNamed("sum")
                .assertTypedAs<NumberType>()
            assert(sum.nativeValue == (0..4).sum().toDouble())
        }

        evaluationOf("""
            var i = 0
            for (; i < 5; i += 1);
        """).assertNormalAnd {
            val i = variableNamed("i")
                .assertTypedAs<NumberType>()
            assert(i.nativeValue == 5.0)
        }

        evaluationOf("""
            var i = 0
            for (; i < 5;) i += 1
        """).assertNormalAnd {
            val i = variableNamed("i")
                .assertTypedAs<NumberType>()
            assert(i.nativeValue == 5.0)
        }

        evaluationOf("""
            var i = 0
            for (;;) {
                i += 1
                if (i == 5) break
            }
        """).assertNormalAnd {
            val i = variableNamed("i")
                .assertTypedAs<NumberType>()
            assert(i.nativeValue == 5.0)
        }
    }
    @Test
    fun testThrow() {
        evaluationOf("""
            throw 0
        """).assertThrowAnd {}

        evaluationOf("""
            while (true) throw 0
        """).assertThrowAnd {}
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
        """).assertNormalAnd {
            val threw = variableNamed("threw")
                .assertTypedAs<BooleanType>()
            assertTrue(threw.nativeValue)
        }

        evaluationOf("""
            var threw = false
            try {
                throw 0
            } finally {
                threw = true
            }
        """).assertThrowAnd {
            val threw = variableNamed("threw")
                .assertTypedAs<BooleanType>()
            assertTrue(threw.nativeValue)
        }

        evaluationOf("""
            try {
                throw 0
            } catch {
                0
            } finally {
                1
            }
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 0.0)
        }

        evaluationOf("""
            try {
                throw 0
            } finally {
                throw 1
            }
        """).assertThrowAnd<NumberType> {
            assert(value.nativeValue == 1.0)
        }
    }
    @Test
    fun testObjectBasic() {
        evaluationOf("""
            ({ a: 0 })
        """).assertNormalAnd<ObjectType> {
            value.dataPropertyNamed("a").run {
                value.assertTypeAnd<NumberType> {
                    assert(nativeValue == 0.0)
                }
            }
        }

        evaluationOf("""
            let a = 0
            let b = { a }
        """).assertNormalAnd {
            val b = variableNamed("b")
                .assertTypedAs<ObjectType>()
            b.dataPropertyNamed("a").run {
                value.assertTypeAnd<NumberType> {
                    assert(nativeValue == 0.0)
                }
            }
        }

        evaluationOf("""
            ({ true })
        """).assertNormalAnd<ObjectType> {
            value.dataPropertyNamed("true").run {
                value.assertTypeAnd<BooleanType> {
                    assertTrue(nativeValue)
                }
            }
        }

        evaluationOf("""
            ({ ["a"] })
        """).assertNormalAnd<ObjectType> {
            value.dataPropertyNamed("a").run {
                value.assertTypeAnd<StringType> {
                    assert(nativeValue == "a")
                }
            }
        }

        evaluationOf("""
            var evaluationCount = 0
            ;({ [evaluationCount += 1] })
        """).assertNormalAnd {
            val evaluationCount = variableNamed("evaluationCount")
                .assertTypedAs<NumberType>()
            assert(evaluationCount.nativeValue == 1.0)
        }
    }
    @Test
    fun testMember() {
        evaluationOf("""
            ({ a: 0 }).a
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 0.0)
        }

        evaluationOf("""
            ({ a: { b: 0 } })?.a.b
        """).assertNormalAnd<NumberType> {
            assert(value.nativeValue == 0.0)
        }

        evaluationOf("""
            null?.a
        """).assertNormalAnd<NullType> {}

        evaluationOf("""
            null?.a.b
        """).assertNormalAnd<NullType> {}
    }
    @Test
    fun testObject() {
        evaluationOf("""
            
        """)
    }
    @Test
    fun testArrowFunction() {
        evaluationOf("""
            let getZero = () => 0
            let zero = getZero()
        """).assertNormalAnd {
            val zero = variableNamed("zero")
                .assertTypedAs<NumberType>()
            assert(zero.nativeValue == 0.0)
        }

        evaluationOf("""
            let addOne = (x) => x + 1
            let two = addOne(1)
        """).assertNormalAnd {
            val two = variableNamed("two")
                .assertTypedAs<NumberType>()
            assert(two.nativeValue == 2.0)
        }

        evaluationOf("""
            let argsAsArr = (...args) => args
            let args = argsAsArr(1, 2, 3)
        """).assertNormalAnd {
            val args = variableNamed("args")
                .assertTypedAs<ArrayType>()
            for (i in 1..3) assert(args.at(i - 1) == NumberType(i.toDouble()))
            }
        }
    }
    @Test
    fun testSyncGenerator() {
        evaluationOf("""
            let createG = gen () => {
                for (var i = 0; ; i += 1) yield i
            }
            let g = createG()
            ;[g.next(), g.next()]
        """).assertNormalAnd<ArrayType> {
            value.at(0).assertTypeAnd<ObjectType> {
                dataPropertyNamed("value").value.assertTypeAnd<NumberType> {
                    assert(nativeValue == 0.0)
                }
            }
            value.at(1).assertTypeAnd<ObjectType> {
                dataPropertyNamed("value").value.assertTypeAnd<NumberType> {
                    assert(nativeValue == 1.0)
                }
            }
        }
    }
}

private fun ArrayType.at(index: Int) =
    array.getOrNull(index)
private fun ObjectType.dataPropertyNamed(name: String) =
    properties[name.languageValue].assertType<DataProperty>()
private fun SourceTextModule.variableNamed(name: String): Binding {
    val binding = env!!.bindings[name]
    assertNotNull(binding)
    return binding
}
private inline fun <reified Value: LanguageType> Binding.assertTypedAs() =
    value.assertType<Value>()
private fun EvaluationResult.assertNormalAnd(block: SourceTextModule.() -> Unit) {
    assertIs<Completion.Normal<*>>(completion)
    block(module)
}
@JvmName("assertNormalTypedAnd")
private inline fun <reified Value: LanguageType> EvaluationResult.assertNormalAnd(
    crossinline block: StrictAssertionContext<Value>.() -> Unit,
) =
    this.assertNormalAnd {
        completion.value.assertTypeAnd<Value> {
            block(StrictAssertionContext(this, module))
        }
    }
private fun EvaluationResult.assertThrowAnd(block: SourceTextModule.() -> Unit) {
    assertIs<Completion.Throw>(completion)
    block(module)
    // TODO: put more assertions
}
@JvmName("assertThrowTypedAnd")
private inline fun <reified Value: LanguageType> EvaluationResult.assertThrowAnd(
    crossinline block: StrictAssertionContext<Value>.() -> Unit,
) =
    this.assertThrowAnd {
        completion.value.assertTypeAnd<Value> {
            block(StrictAssertionContext(this, module))
        }
    }
private val config = object: HostConfig() {
    override fun loadImportedModule(module: CyclicModule, specifier: String, state: GraphLoadingState) = TODO()
    override fun wait(ms: Int) = PromiseType.resolve(NullType)
    override fun onGotUncaughtAbrupt(abrupt: Completion.Abrupt) = TODO()
    override fun display(value: LanguageType, raw: Boolean) = TODO()
}
private fun evaluationOf(code: String): EvaluationResult {
    HostConfig.set(config)
    initializeRealm()
    return when (val moduleOrError = parseModule(code.trimIndent(), runningExecutionContext.realm)) {
        is Valid -> {
            val module = moduleOrError.value
            val res = run {
                module.initializeEnvironment()
                    .orReturn { return@run it }
                module.executeModuleWithoutIgnoringValue()
            }
            EvaluationResult(res, module)
        }
        is Invalid -> throw moduleOrError.value
    }
}
/**
 * @see SourceTextModule.execute
 */
private fun SourceTextModule.executeModuleWithoutIgnoringValue(): MaybeEmptyOrAbrupt {
    executionContextStack.addTop(ExecutionContext(realm, env))
    val res = node.evaluate()
        .unwrap()
    runJobs()
    executionContextStack.removeTop()
    return res
}
private data class EvaluationResult(val completion: Completion<*>, val module: SourceTextModule)
private data class StrictAssertionContext<V: LanguageType>(val value: V, val module: SourceTextModule)
