package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.type.NormalOrAbrupt

sealed interface StatementNode: Node {
    override fun evaluate(): NormalOrAbrupt
}
