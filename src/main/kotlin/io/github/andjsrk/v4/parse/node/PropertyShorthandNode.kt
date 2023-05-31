package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class PropertyShorthandNode(val key: ObjectLiteralKeyNode): ObjectElementNode {
    override val childNodes get() = listOf(key)
    override val range = key.range
    override fun toString() =
        stringifyLikeDataClass(::key, ::range)
}
