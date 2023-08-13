package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ObjectGetterNode(
    override val name: ObjectLiteralKeyNode,
    override val body: BlockNode,
    startRange: Range,
): GetterNode, ObjectElementNode {
    override val childNodes get() = listOf(name, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::name, ::body, ::range)
    override fun evaluate() = TODO()
}
