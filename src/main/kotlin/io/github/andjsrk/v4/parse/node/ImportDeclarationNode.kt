package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.type.empty

sealed interface ImportDeclarationNode: DeclarationNode {
    val moduleSpecifier: StringLiteralNode
    override fun evaluate() = empty
}
