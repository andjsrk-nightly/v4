package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class YieldNode(
    val expression: ExpressionNode,
    val isSpread: Boolean,
    startRange: Range,
): ExpressionNode, NonAtomicNode {
    override val childNodes get() = listOf(expression)
    override val range = startRange..expression.range
    override fun toString() =
        stringifyLikeDataClass(::expression, ::isSpread, ::range)
    override fun evaluate() =
        EvalFlow {
            val exprValue = expression.evaluateValue()
                .returnIfAbrupt(this) { return@EvalFlow }
            if (!isSpread) `return`(yieldAll(commonYield(exprValue)))
            else {
                val genKind = generatorKind
                val iterRecord = IteratorRecord.from(exprValue, genKind)
                    .returnIfAbrupt { `return`(it) }
                val iter = iterRecord.sourceObject
                var received: NonEmptyNormalOrAbrupt = `null`
                while (true) {
                    when (received) {
                        is Completion.Normal -> {
                            val nextRes = iterRecord.nextMethod._call(iter, listOf(received.value))
                                .returnIfAbrupt { `return`(it) }
                                .requireToBe<ObjectType> { `return`(it) }
                            val done = nextRes.isDoneIterResult { `return`(it) }
                            if (done) `return`(nextRes.extractIterResultValue())
                            received =
                                if (genKind == GeneratorKind.ASYNC) TODO()
                                else yieldAll(syncYield(nextRes)) ?: neverHappens()
                        }
                        is Completion.Throw -> {
                            val throwMethod = iter.getMethod("throw".languageValue)
                                .returnIfAbrupt { `return`(it) }
                                ?.normalizeNull()
                                ?.requireToBe<FunctionType> { `return`(it) }
                            if (throwMethod != null) {
                                val throwRes = throwMethod._call(iter, listOf(received.value))
                                    .returnIfAbrupt { `return`(it) }
                                    .awaitIfAsyncGenerator(genKind) { `return`(it) }
                                    .requireToBe<ObjectType> { `return`(it) }
                                val done = throwRes.isDoneIterResult { `return`(it) }
                                if (done) `return`(throwRes.extractIterResultValue())
                                received =
                                    if (generatorKind == GeneratorKind.ASYNC) TODO()
                                    else yieldAll(syncYield(throwRes)) ?: neverHappens()
                            } else {
                                if (genKind == GeneratorKind.ASYNC) TODO()
                                else iterRecord.close(empty)
                                `return`(throwError(TypeErrorKind.THROW_METHOD_MISSING))
                            }
                        }
                        is Completion.Return -> {
                            val returnMethod = iter.getMethod("return".languageValue)
                                .returnIfAbrupt { `return`(it) }
                                ?.normalizeNull()
                                ?.requireToBe<FunctionType> { `return`(it) }
                            if (returnMethod == null) {
                                val value = received.value
                                    .awaitIfAsyncGenerator(genKind) { `return`(it) }
                                `return`(Completion.Return(value))
                            }
                            val returnRes = returnMethod._call(iter, listOf(received.value))
                                .returnIfAbrupt { `return`(it) }
                                .awaitIfAsyncGenerator(genKind) { `return`(it) }
                                .requireToBe<ObjectType> { `return`(it) }
                            val done = returnRes.isDoneIterResult { `return`(it) }
                            if (done) {
                                val value = iter.extractIterResultValue()
                                    .returnIfAbrupt { `return`(it) }
                                `return`(Completion.Return(value))
                            }
                            received =
                                if (genKind == GeneratorKind.ASYNC) TODO()
                                else yieldAll(syncYield(returnRes)) ?: neverHappens()
                        }
                        else -> missingBranch()
                    }
                }
            }
        }
}

private inline fun LanguageType.awaitIfAsyncGenerator(kind: GeneratorKind, rtn: AbruptReturnLambda) =
    if (kind == GeneratorKind.ASYNC) TODO()
    else this
