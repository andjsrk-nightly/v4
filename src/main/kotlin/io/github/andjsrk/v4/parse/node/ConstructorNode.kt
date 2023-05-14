package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ConstructorNode(
    override val name: ObjectLiteralKeyNode,
    override val parameters: FormalParametersNode,
    override val body: BlockStatementNode,
): ClassElementNode, MethodNode {
    override val childNodes = listOf(name, parameters, body)
    override val range = name.range..body.range
    override fun toString() =
        stringifyLikeDataClass(::name, ::parameters, ::body)
}
