package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class BlockNode(
    override val elements: List<StatementNode>,
    override val range: Range,
): StatementNode, StatementListNode {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate(): Completion {
        if (elements.isEmpty()) return Completion.empty

        val oldEnv = runningExecutionContext.lexicalEnvironment
        val blockEnv = DeclarativeEnvironment(oldEnv)
        instantiateBlockDeclaration(this, blockEnv)
        runningExecutionContext.lexicalEnvironment = blockEnv
        val res = evaluateStatements()
        runningExecutionContext.lexicalEnvironment = oldEnv
        return res
    }
}
