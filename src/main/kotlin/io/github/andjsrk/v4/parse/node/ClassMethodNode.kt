package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ClassMethodNode(
    override val name: ObjectLiteralKeyNode,
    override val parameters: FormalParametersNode,
    override val body: BlockStatementNode,
    override val isAsync: Boolean,
    override val isGenerator: Boolean,
    override val isStatic: Boolean,
    startRange: Range,
): MethodNode, ClassElementNode {
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::name, ::parameters, ::body, ::isAsync, ::isGenerator, ::isStatic, ::range)
}
