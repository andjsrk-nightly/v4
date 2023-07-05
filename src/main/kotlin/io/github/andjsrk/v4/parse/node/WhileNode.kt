package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*
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
    override fun evaluateLoop(): NonEmptyNormalOrAbrupt {
        var res: LanguageType = NullType
        if (atLeastOnce) res = body.evaluate().returnIfShouldNotContinue(res) { return it }
        while (true) {
            val testVal = test.evaluateValueOrReturn { return it }
                .requireToBe<BooleanType> { return it }
            if (!testVal.value) return Completion.Normal(res)
            res = body.evaluate().returnIfShouldNotContinue(res) { return it }
        }
    }
}
