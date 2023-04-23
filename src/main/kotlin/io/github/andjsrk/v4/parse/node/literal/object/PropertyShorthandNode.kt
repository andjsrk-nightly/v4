package io.github.andjsrk.v4.parse.node.literal.`object`

import io.github.andjsrk.v4.parse.node.IdentifierNode
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class PropertyShorthandNode(val identifier: IdentifierNode): ObjectElementNode {
    override val range = identifier.range
    override fun toString() =
        stringifyLikeDataClass(::identifier, ::range)
}
