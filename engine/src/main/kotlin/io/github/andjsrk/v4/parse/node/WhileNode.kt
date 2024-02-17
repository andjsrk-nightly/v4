package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class WhileNode(
    val test: ExpressionNode,
    override val body: StatementNode,
    val atLeastOnce: Boolean,
    startRange: Range,
): IterationStatementNode {
    override val childNodes get() = listOf(test, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::test, ::body, ::atLeastOnce, ::range)
    override fun evaluateLoop() = lazyFlow f@ {
        var res: LanguageType = NullType
        if (atLeastOnce) {
            res = yieldAll(body.evaluate())
                .returnIfShouldNotContinue(res) { return@f it }
        }
        while (true) {
            val testVal = yieldAll(test.evaluateValue())
                .orReturn { return@f it }
                .requireToBe<BooleanType> { return@f it }
            if (!testVal.value) return@f res.toNormal()
            res = yieldAll(body.evaluate())
                .returnIfShouldNotContinue(res) { return@f it }
        }
        @CompilerFalsePositive
        neverHappens()
    }
}
