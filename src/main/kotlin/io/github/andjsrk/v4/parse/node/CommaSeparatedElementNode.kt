package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.node.literal.`object`.ObjectElementNode
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class CommaSeparatedElementNode<N: Node>(
    val node: N,
    val isSpread: Boolean,
    override val range: Range,
): ObjectElementNode {
    override fun toString() =
        stringifyLikeDataClass(::node, ::isSpread, ::range)
}
