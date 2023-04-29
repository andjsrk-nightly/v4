package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ObjectBindingPatternNode(
    override val elements: List<MaybeRestNode>,
    override val range: Range,
): BindingPatternNode {
    override fun toString() =
        stringifyLikeDataClass(::elements, ::range)
}
