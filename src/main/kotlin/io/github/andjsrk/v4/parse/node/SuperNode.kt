package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class SuperNode(override val range: Range): ExpressionNode/* for compatibility */ {
    override fun toString() =
        stringifyLikeDataClass(::range)
}
