package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.evaluateValueOrReturn
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.spec.Completion

class IfExpressionNode(
    test: ExpressionNode,
    then: ExpressionNode,
    override val `else`: ExpressionNode,
    startRange: Range
): IfNode<ExpressionNode>(test, then, `else`), ExpressionNode {
    override val range = startRange..`else`.range
    override fun evaluate(): Completion {
        val testVal = test.evaluateValueOrReturn { return it }
        if (testVal !is BooleanType) return Completion(Completion.Type.THROW, NullType/* TypeError */)
        return Completion.normal(
            if (testVal.value) then.evaluateValueOrReturn { return it }
            else `else`.evaluateValueOrReturn { return it }
        )
    }
}
