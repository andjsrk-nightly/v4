package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class BreakNode(startRange: Range, semicolonRange: Range?): StatementNode {
    override val range = startRange..(semicolonRange ?: startRange)
    override fun toString() =
        stringifyLikeDataClass(::range)
}
