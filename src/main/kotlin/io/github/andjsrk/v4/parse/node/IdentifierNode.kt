package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class IdentifierNode(
    val value: String,
    override val range: Range,
): ExpressionNode, ObjectLiteralKeyNode {
    override fun toString() =
        stringifyLikeDataClass(::value, ::range)
}
