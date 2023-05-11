package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class FieldNode(
    val name: ObjectLiteralKeyNode,
    val default: ExpressionNode,
    override val isStatic: Boolean,
    startRange: Range,
): NormalClassElementNode {
    override val childNodes = listOf(name, default)
    override val range = startRange..default.range
    override fun toString() =
        stringifyLikeDataClass(::name, ::default, ::isStatic, ::range)
}
