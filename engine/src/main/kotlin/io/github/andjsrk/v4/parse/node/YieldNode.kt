package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
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
    override fun evaluate() = lazyFlow f@ {
        val value = yieldAll(expression.evaluateValue())
            .orReturn { return@f it }
        if (isSpread) {
            val iter = IteratorRecord.from(value)
                .orReturn { return@f it }
            var received: NonEmptyOrAbrupt = normalNull
            while (true) {
                when (received) {
                    is Completion.Normal -> {
                        val res = iter.next()
                            .orReturn { return@f it }
                        val done = res.getDone()
                            .orReturn { return@f it }
                            .value
                        val resValueComp = res.getValue()
                        val resValue = resValueComp
                            .orReturn { return@f it }
                        if (done) return@f resValueComp
                        received = yieldAll(commonYield(resValue))
                    }
                    is Completion.Return -> {
                        val closeMethod = iter.sourceObject.getMethod("close".languageValue)
                            .orReturn { return@f it }
                            ?: return@f received // TODO: await its value if needed
                        val closeRes = closeMethod.call(iter.sourceObject, listOf(received.value))
                            .orReturn { return@f it }
                            // TODO: await its value if needed
                            .requireToBe<ObjectType> { return@f it }
                            .asIteratorResult()
                        val done = closeRes.getDone()
                            .orReturn { return@f it }
                            .value
                        val resValue = closeRes.getValue()
                            .orReturn { return@f it }
                        if (done) return@f Completion.Return(resValue)
                        received = yieldAll(commonYield(resValue))
                    }
                    else -> received.orReturn { return@f it }
                }
            }
            @CompilerFalsePositive
            neverHappens()
        } else yieldAll(commonYield(value))
    }
}
