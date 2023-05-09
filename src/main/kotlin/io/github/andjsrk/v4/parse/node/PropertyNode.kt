package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class PropertyNode(
    val key: ObjectLiteralKeyNode,
    val value: ExpressionNode,
): ObjectElementNode {
    override val range = key.range..value.range
    override fun toString() =
        stringifyLikeDataClass(::key, ::value, ::range)
}
