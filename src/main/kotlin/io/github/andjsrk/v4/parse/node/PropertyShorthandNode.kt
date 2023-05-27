package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class PropertyShorthandNode(val name: ObjectLiteralKeyNode): ObjectElementNode {
    override val childNodes get() = listOf(name)
    override val range = name.range
    override fun toString() =
        stringifyLikeDataClass(::name, ::range)
}
