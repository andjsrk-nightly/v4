package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.LexicalDeclarationKind
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NormalLexicalDeclarationNode(
    override val kind: LexicalDeclarationKind,
    val bindings: List<LexicalBindingNode>,
    startRange: Range,
    semicolonRange: Range?,
): LexicalDeclarationNode {
    override val childNodes = bindings
    override val range = startRange..bindings.last().range.extendCarefully(semicolonRange)
    override fun toString() =
        stringifyLikeDataClass(::kind, ::bindings, ::range)
}
