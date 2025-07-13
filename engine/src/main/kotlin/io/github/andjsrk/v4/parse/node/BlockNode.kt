package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

// NOTE: Block as a statement is removed, but it is still used for syntaxes such as methods
class BlockNode(
    override val elements: List<StatementNode>,
    override val range: Range,
): StatementNode, StatementListNode, ConciseBodyNode {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate() = lazyFlow f@ {
        if (elements.isEmpty()) return@f empty

        val blockEnv = DeclarativeEnvironment()
        instantiateBlockDeclaration(this@BlockNode, blockEnv)
        withTemporalLexicalEnv(blockEnv) {
            yieldAll(evaluateStatements(this@BlockNode))
        }
    }
}
