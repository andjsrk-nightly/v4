package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.LexicalDeclarationKind

interface LexicalDeclarationNode: DeclarationNode {
    val kind: LexicalDeclarationKind
}
