package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class RestNode(
    override val binding: IdentifierOrBindingPatternNode,
    override val range: Range,
): MaybeRestNode {
    override val childNodes = listOf(binding)
    override fun toString() =
        stringifyLikeDataClass(::binding, ::range)
}