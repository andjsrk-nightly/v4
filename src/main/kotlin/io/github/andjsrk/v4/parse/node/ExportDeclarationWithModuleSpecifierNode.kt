package io.github.andjsrk.v4.parse.node

sealed interface ExportDeclarationWithModuleSpecifierNode: ExportDeclarationNode {
    val moduleSpecifier: StringLiteralNode
}
