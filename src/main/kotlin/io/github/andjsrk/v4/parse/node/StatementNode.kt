package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.type.MaybeEmptyOrAbrupt

sealed interface StatementNode: Node {
    override fun evaluate(): MaybeEmptyOrAbrupt
}
