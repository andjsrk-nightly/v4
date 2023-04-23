package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ProgramNode(override val statements: List<StatementNode>): BlockNode {
    override val range = Range(0, statements.lastOrNull()?.range?.end ?: 0)
    override fun toString() =
        stringifyLikeDataClass(::statements, ::range)
}
