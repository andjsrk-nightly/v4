package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.type.NonEmpty

sealed interface ExpressionNode: Node, ConciseBodyNode {
    override fun evaluate(): NonEmpty = TODO() // temp
}
