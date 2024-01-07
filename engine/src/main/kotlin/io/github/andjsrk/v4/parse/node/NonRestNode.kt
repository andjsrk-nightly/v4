package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

open class NonRestNode(
    override val binding: BindingElementNode,
    val default: ExpressionNode?,
): MaybeRestNode {
    override val childNodes get() = listOf(binding, default)
    override val range by lazy { binding.range..(default ?: binding).range }
    override fun toString() =
        stringifyLikeDataClass(::binding, ::default, ::range)
}
