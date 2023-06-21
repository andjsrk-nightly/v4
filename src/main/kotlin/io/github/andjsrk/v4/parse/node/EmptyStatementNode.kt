package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class EmptyStatementNode(override val range: Range): StatementNode, ClassElementNode {
    override fun toString() =
        stringifyLikeDataClass(::range)
    override fun evaluate() = Completion.Normal.empty
}
