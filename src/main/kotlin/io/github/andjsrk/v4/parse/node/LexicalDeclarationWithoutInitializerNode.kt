package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.LexicalDeclarationKind
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

open class LexicalDeclarationWithoutInitializerNode(
    val kind: LexicalDeclarationKind,
    val binding: IdentifierOrBindingPatternNode,
    startRange: Range,
): DeclarationNode {
    override val childNodes: List<Node?> = listOf(binding)
    override val range = startRange..binding.range
    override fun toString() =
        stringifyLikeDataClass(::kind, ::binding, ::range)
}
