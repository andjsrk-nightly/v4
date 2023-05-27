package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NormalForNode(
    val init: LexicalDeclarationNode?,
    val test: ExpressionNode?,
    val update: ExpressionNode?,
    override val body: StatementNode,
    startRange: Range,
): ForNode {
    override val childNodes get() = listOf(init, test, update, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::init, ::test, ::update, ::body, ::range)
}
