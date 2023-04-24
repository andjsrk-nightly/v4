package io.github.andjsrk.v4.parse.node.literal.`object`

import io.github.andjsrk.v4.parse.node.IdentifierNode
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class PropertyShorthandNode(val name: IdentifierNode): ObjectElementNode {
    override val range = name.range
    override fun toString() =
        stringifyLikeDataClass(::name, ::range)
}
