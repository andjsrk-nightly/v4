package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.evaluateValueOrReturn
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.evaluate.updateEmpty

class IfStatementNode(
    test: ExpressionNode,
    then: StatementNode,
    `else`: StatementNode?,
    startRange: Range,
): IfNode<StatementNode>(test, then, `else`), StatementNode {
    override val range = startRange..(`else` ?: then).range
    override fun evaluate(): Completion {
        val testVal = test.evaluateValueOrReturn { return it }
        if (testVal !is BooleanType) return Completion.`throw`(NullType/* TypeError */)
        val completion =
            if (testVal.value) then.evaluate()
            else `else`?.evaluate() ?: Completion.empty
        return updateEmpty(completion, NullType)
    }
}
