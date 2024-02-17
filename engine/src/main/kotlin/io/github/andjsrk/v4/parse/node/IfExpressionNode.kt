package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.BooleanType

class IfExpressionNode(
    test: ExpressionNode,
    then: ExpressionNode,
    override val `else`: ExpressionNode,
    startRange: Range
): IfNode<ExpressionNode>(test, then, `else`), ExpressionNode {
    override val range = startRange..`else`.range
    override fun evaluate() = lazyFlow f@ {
        val testVal = yieldAll(test.evaluateValue())
            .orReturn { return@f it }
            .requireToBe<BooleanType> { return@f it }
        yieldAll(
            if (testVal.value) then.evaluateValue()
            else `else`.evaluateValue()
        )
    }
}
