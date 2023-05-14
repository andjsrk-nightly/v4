package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ForInNode(
    val declaration: LexicalDeclarationWithoutInitializerNode,
    val body: StatementNode,
    startRange: Range,
): ForNode {
    override val childNodes = listOf(declaration, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::declaration, ::body)
}
