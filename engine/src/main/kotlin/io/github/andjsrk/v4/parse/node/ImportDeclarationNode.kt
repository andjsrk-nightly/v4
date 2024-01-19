package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.lazyFlowNoYields
import io.github.andjsrk.v4.evaluate.type.SourceTextModule
import io.github.andjsrk.v4.evaluate.type.empty

sealed interface ImportDeclarationNode: DeclarationNode {
    val moduleSpecifier: StringLiteralNode
    /**
     * @see SourceTextModule
     */
    override fun evaluate() = lazyFlowNoYields { empty }
}
