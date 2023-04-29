package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ComputedObjectKeyNode(
    val expression: ExpressionNode,
    override val range: Range,
): ObjectLiteralKeyNode {
    override fun toString() =
        stringifyLikeDataClass(::expression, ::range)
}
