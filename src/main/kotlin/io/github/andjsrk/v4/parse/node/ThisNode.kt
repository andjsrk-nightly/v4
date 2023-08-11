package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ThisNode(override val range: Range): ExpressionNode {
    override fun toString() =
        stringifyLikeDataClass(::range)
    override fun evaluate() = TODO()
}
