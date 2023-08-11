package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.EvalFlow
import io.github.andjsrk.v4.evaluate.type.AbstractType

sealed interface ExpressionNode: Node, ConciseBodyNode {
    override fun evaluate(): EvalFlow<AbstractType>
}
