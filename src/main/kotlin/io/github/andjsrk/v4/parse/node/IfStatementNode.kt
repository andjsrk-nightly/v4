package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
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
    override fun evaluate(): Completion<*> {
        val testVal = test.evaluateValueOrReturn { return it }
            .requireToBe<BooleanType> { return it }
        val completion =
            if (testVal.value) then.evaluate()
            else `else`?.evaluate() ?: empty
        return updateEmpty(completion, NullType)
    }
}
