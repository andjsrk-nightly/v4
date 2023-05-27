package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ObjectSetterNode(
    override val name: ObjectLiteralKeyNode,
    override val parameter: NonRestNode,
    override val body: BlockNode,
    startRange: Range,
): SetterNode, ObjectElementNode {
    override val childNodes get() = listOf(name, parameter, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::name, ::parameter, ::body, ::range)
}
