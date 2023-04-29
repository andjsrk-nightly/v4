package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NonRestObjectPropertyNode(
    val key: ObjectLiteralKeyNode,
    `as`: Node?,
    default: ExpressionNode?,
): NonRestNode(`as` ?: key, default) {
    override fun toString() =
        stringifyLikeDataClass(::key, ::`as`, ::default, ::range)
}
