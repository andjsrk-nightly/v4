package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ObjectMethodNode(
    override val name: ObjectLiteralKeyNode,
    override val parameters: FormalParametersNode,
    override val body: BlockStatementNode,
    override val isAsync: Boolean,
    override val isGenerator: Boolean,
    startRange: Range,
): MethodNode, ObjectElementNode {
    override val childNodes = listOf(name, parameters, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::name, ::parameters, ::body, ::isAsync, ::isGenerator, ::range)
}
