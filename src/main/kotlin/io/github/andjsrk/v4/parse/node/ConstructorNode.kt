package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ConstructorNode(
    override val name: ObjectLiteralKeyNode,
    override val parameters: UniqueFormalParametersNode,
    override val body: BlockNode,
): ClassElementNode, NonSpecialMethodNode {
    override val childNodes get() = listOf(name, parameters, body)
    override val range = name.range..body.range
    override fun toString() =
        stringifyLikeDataClass(::name, ::parameters, ::body, ::range)
}
