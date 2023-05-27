package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.LexicalDeclarationKind
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

/**
 * This node will only be used for special context that can never have initializer, such as head of `for ... in` statement.
 * `let x` in normal context will be treated as [LexicalDeclarationNode] with [LexicalDeclarationNode.value] is set to `null`.
 */
open class LexicalDeclarationWithoutInitializerNode(
    val kind: LexicalDeclarationKind,
    val binding: BindingElementNode,
    startRange: Range,
): DeclarationNode {
    override val childNodes get() = listOf<Node?>(binding)
    override val range = startRange..binding.range
    override fun toString() =
        stringifyLikeDataClass(::kind, ::binding, ::range)
}
