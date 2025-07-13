package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.instantiateBlockDeclaration
import io.github.andjsrk.v4.evaluate.lazyFlow
import io.github.andjsrk.v4.evaluate.orReturn
import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.evaluate.type.normalNull
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.evaluate.withTemporalLexicalEnv
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class DoExpressionNode(override val elements: List<StatementNode>, override val range: Range): ExpressionNode, StatementListNode {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass()
    override fun evaluate() = lazyFlow f@ {
        if (elements.isEmpty()) return@f normalNull

        val blockEnv = DeclarativeEnvironment()
        instantiateBlockDeclaration(this@DoExpressionNode, blockEnv)
        withTemporalLexicalEnv(blockEnv) {
            val lastStmtRes = elements.map {
                yieldAll(it.evaluate())
                    .orReturn { return@f it }
            }
                .lastOrNull()
            lastStmtRes?.toNormal() ?: normalNull
        }
    }
}
