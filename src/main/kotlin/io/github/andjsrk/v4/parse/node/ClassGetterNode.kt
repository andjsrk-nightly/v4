package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ClassGetterNode(
    override val name: ObjectLiteralKeyNode,
    override val body: BlockStatementNode,
    override val isStatic: Boolean,
    startRange: Range,
): GetterNode, NormalClassElementNode {
    override val childNodes = listOf(name, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::name, ::body, ::isStatic, ::range)
}
