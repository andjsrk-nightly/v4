package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType
import io.github.andjsrk.v4.evaluate.type.lang.NullType

class IfStatementNode(
    test: ExpressionNode,
    then: StatementNode,
    `else`: StatementNode?,
    startRange: Range,
): IfNode<StatementNode>(test, then, `else`), StatementNode {
    override val range = startRange..(`else` ?: then).range
    override fun evaluate() = lazyFlow f@ {
        val testVal = yieldAll(test.evaluateValue())
            .orReturn { return@f it }
            .requireToBe<BooleanType> { return@f it }
        val completion =
            if (testVal.value) yieldAll(then.evaluate())
            else `else`?.evaluate()?.let { yieldAll(it) } ?: empty
        updateEmpty(completion, NullType)
    }
}
