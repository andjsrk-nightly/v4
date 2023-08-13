package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class RestNode(
    override val binding: BindingElementNode,
    override val range: Range,
): MaybeRestNode {
    override val childNodes get() = listOf(binding)
    override fun toString() =
        stringifyLikeDataClass(::binding, ::range)
    override fun evaluate() = TODO()
}
