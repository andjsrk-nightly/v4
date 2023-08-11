package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.LexicalDeclarationKind
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

/**
 * This node will only be used for special context that can never have initializer, such as head of `for ... in` statement.
 * `let x` in normal context will be treated as [NormalLexicalDeclarationNode] with [NormalLexicalDeclarationNode.bindings] is set to an empty list.
 */
class LexicalDeclarationWithoutInitializerNode(
    override val kind: LexicalDeclarationKind,
    val binding: BindingElementNode,
    startRange: Range,
): LexicalDeclarationNode {
    override val childNodes get() = listOf<Node?>(binding)
    override val range = startRange..binding.range
    override fun toString() =
        stringifyLikeDataClass(::kind, ::binding, ::range)
    override fun evaluate() = TODO()
}
