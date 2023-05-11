package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

typealias IdentifierOrBindingPatternNode = Node

open class NonRestNode(
    override val binding: IdentifierOrBindingPatternNode,
    val default: ExpressionNode?,
): MaybeRestNode {
    override val childNodes by lazy { listOf(binding, default) }
    override val range by lazy { binding.range..(default ?: binding).range }
    override fun toString() =
        stringifyLikeDataClass(::binding, ::default, ::range)
}
