package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ObjectGetterNode(
    override val name: ObjectLiteralKeyNode,
    override val body: BlockStatementNode,
    startRange: Range,
): GetterNode, ObjectElementNode {
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::name, ::body, ::range)
}
