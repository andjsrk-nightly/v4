package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ArgumentsNode(
    val elements: List<MaybeSpreadNode>,
    override val range: Range,
): NonAtomicNode {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate() = TODO()
}
