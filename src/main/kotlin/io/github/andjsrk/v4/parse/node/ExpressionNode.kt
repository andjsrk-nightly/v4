package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.type.NonEmptyWideOrAbrupt

sealed interface ExpressionNode: Node, ConciseBodyNode {
    override fun evaluate(): NonEmptyWideOrAbrupt
}
