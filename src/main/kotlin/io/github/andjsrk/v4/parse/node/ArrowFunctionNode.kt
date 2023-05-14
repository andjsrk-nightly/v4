package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ArrowFunctionNode(
    override val parameters: FormalParametersNode,
    override val body: Node,
    override val isAsync: Boolean,
    override val isGenerator: Boolean,
    startRange: Range,
): NormalFunctionNode, ExpressionNode {
    init {
        require(body is ExpressionNode || body is BlockStatementNode)
    }
    override val childNodes = listOf(parameters, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::parameters, ::body, ::isAsync, ::isGenerator, ::range)
}
