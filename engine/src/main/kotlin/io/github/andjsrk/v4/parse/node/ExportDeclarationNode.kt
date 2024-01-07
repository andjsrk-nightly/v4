package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.type.*

sealed interface ExportDeclarationNode: DeclarationNode {
    /**
     * @see SourceTextModule
     */
    override fun evaluate(): MaybeEmptyOrAbrupt = empty
}
