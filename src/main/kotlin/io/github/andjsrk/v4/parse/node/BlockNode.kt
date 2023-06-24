package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class BlockNode(
    override val elements: List<StatementNode>,
    override val range: Range,
): StatementNode, StatementListNode, ConciseBodyNode {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate(): NormalOrAbrupt {
        if (elements.isEmpty()) return empty

        val oldEnv = runningExecutionContext.lexicalEnvironment
        val blockEnv = DeclarativeEnvironment(oldEnv)
        instantiateBlockDeclaration(this, blockEnv)
        runningExecutionContext.lexicalEnvironment = blockEnv
        val res = evaluateStatements()
        runningExecutionContext.lexicalEnvironment = oldEnv
        return res
    }
}
