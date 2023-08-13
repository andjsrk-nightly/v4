package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ImportNode(override val range: Range): ExpressionNode/* for compatibility */ {
    override fun toString() =
        stringifyLikeDataClass(::range)
    override fun evaluate() = TODO()
}
