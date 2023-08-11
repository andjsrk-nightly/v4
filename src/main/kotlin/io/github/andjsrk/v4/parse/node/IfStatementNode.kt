package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.evaluate.type.lang.*

class IfStatementNode(
    test: ExpressionNode,
    then: StatementNode,
    `else`: StatementNode?,
    startRange: Range,
): IfNode<StatementNode>(test, then, `else`), StatementNode {
    override val range = startRange..(`else` ?: then).range
    override fun evaluate() =
        EvalFlow<LanguageType?> {
            val testVal = test.evaluateValue()
                .returnIfAbrupt(this) { return@EvalFlow }
                .requireToBe<BooleanType> { `return`(it) }
            val completion =
                if (testVal.value) yieldAll(then.evaluate()) ?: empty
                else `else`?.evaluate()?.let { yieldAll(it) } ?: empty
            `return`(updateEmpty(completion, NullType))
        }
}
