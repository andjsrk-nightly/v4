package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.lazyFlowNoYields
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class EmptyStatementNode(override val range: Range): StatementNode, ClassElementNode {
    override fun toString() =
        stringifyLikeDataClass(::range)
    override fun evaluate() = lazyFlowNoYields { empty }
}
