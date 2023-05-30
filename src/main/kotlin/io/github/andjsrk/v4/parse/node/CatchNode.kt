package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class CatchNode(
    val binding: BindingElementNode?,
    val body: BlockNode,
    startRange: Range,
): NonAtomicNode {
    override val childNodes get() = listOf(binding, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::binding, ::body, ::range)
}
