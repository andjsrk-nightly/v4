package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class LexicalBindingNode(
    val binding: BindingElementNode,
    val value: ExpressionNode?,
): NonAtomicNode {
    override val childNodes get() = listOf(binding, value)
    override val range = binding.range.extendCarefully(value?.range)
    override fun toString() =
        stringifyLikeDataClass(::binding, ::value, ::range)
    override fun evaluate() = TODO()
}
