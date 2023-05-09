package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class RestNode(
    override val `as`: IdentifierOrBindingPatternNode,
    override val range: Range,
): MaybeRestNode {
    override fun toString() =
        stringifyLikeDataClass(::`as`, ::range)
}
