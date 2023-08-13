package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ClassSetterNode(
    override val name: ObjectLiteralKeyNode,
    override val parameter: NonRestNode,
    override val body: BlockNode,
    override val isStatic: Boolean,
    startRange: Range,
): SetterNode, NormalClassElementNode {
    override val childNodes get() = listOf(name, parameter, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::name, ::parameter, ::body, ::isStatic, ::range)
    override fun evaluate() = TODO()
}
