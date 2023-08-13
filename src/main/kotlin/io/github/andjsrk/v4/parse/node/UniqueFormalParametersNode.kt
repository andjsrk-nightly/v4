package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class UniqueFormalParametersNode(
    val elements: List<MaybeRestNode>,
    override val range: Range,
): NonAtomicNode, ExpressionNode/* for compatibility */ {
    override val childNodes = elements
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
    override fun evaluate() = TODO()
}
