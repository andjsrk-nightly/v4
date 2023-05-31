package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.LexicalDeclarationKind
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class LexicalDeclarationNode(
    kind: LexicalDeclarationKind,
    binding: BindingElementNode,
    val value: ExpressionNode?,
    startRange: Range,
    semicolonRange: Range?,
): LexicalDeclarationWithoutInitializerNode(kind, binding, startRange) {
    override val childNodes get() = super.childNodes + value
    override val range = startRange..(value?.range ?: binding.range).extendCarefully(semicolonRange)
    override fun toString() =
        stringifyLikeDataClass(::kind, ::binding, ::value, ::range)
}
