package io.github.andjsrk.v4.parse.node

sealed interface ImportDeclarationNode: DeclarationNode {
    val moduleSpecifier: StringLiteralNode
}
