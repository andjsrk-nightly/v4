package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.evaluate.type.spec.DeclarativeEnvironment
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class BlockNode(
    override val elements: List<StatementNode>,
    override val range: Range,
): StatementNode, StatementListNode {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate(): Completion {
        val oldEnv = Evaluator.runningExecutionContext.lexicalEnvironment
        val blockEnv = DeclarativeEnvironment(oldEnv)
        instantiateBlockDeclaration(this, blockEnv)
        Evaluator.runningExecutionContext.lexicalEnvironment = blockEnv
        val value = elements
            .map { it.evaluate() }
            .reduceRight(::updateEmpty)
        Evaluator.runningExecutionContext.lexicalEnvironment = oldEnv
        return value
    }
}
