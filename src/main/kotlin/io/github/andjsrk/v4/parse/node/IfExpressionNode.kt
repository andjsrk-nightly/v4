package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType

class IfExpressionNode(
    test: ExpressionNode,
    then: ExpressionNode,
    override val `else`: ExpressionNode,
    startRange: Range
): IfNode<ExpressionNode>(test, then, `else`), ExpressionNode {
    override val range = startRange..`else`.range
    override fun evaluate(): NonEmptyNormalOrAbrupt {
        val testVal = test.evaluateValue().orReturn { return it }
            .requireToBe<BooleanType> { return it }
        return (
            if (testVal.value) then.evaluateValue()
            else `else`.evaluateValue()
        )
    }
}
