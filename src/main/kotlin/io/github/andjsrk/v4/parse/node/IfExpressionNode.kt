package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.evaluateValue
import io.github.andjsrk.v4.evaluate.evaluateValueOrReturn
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType
import io.github.andjsrk.v4.evaluate.type.lang.NullType

class IfExpressionNode(
    test: ExpressionNode,
    then: ExpressionNode,
    override val `else`: ExpressionNode,
    startRange: Range
): IfNode<ExpressionNode>(test, then, `else`), ExpressionNode {
    override val range = startRange..`else`.range
    override fun evaluate(): NonEmptyNormalOrAbrupt {
        val testVal = test.evaluateValueOrReturn { return it }
        if (testVal !is BooleanType) return Completion.Throw(NullType/* TypeError */)
        return (
            if (testVal.value) then.evaluateValue()
            else `else`.evaluateValue()
        )
    }
}
