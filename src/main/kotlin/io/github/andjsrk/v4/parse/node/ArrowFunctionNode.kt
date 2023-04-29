package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ArrowFunctionNode(
    parameters: List<MaybeRestNode>,
    body: Node,
    isAsync: Boolean,
    isGenerator: Boolean,
    override val range: Range,
): FunctionNode(parameters, body, isAsync, isGenerator), ExpressionNode {
    init {
        require(body is ExpressionNode || body is BlockStatementNode)
    }
    override fun toString() =
        stringifyLikeDataClass(::parameters, ::body, ::isAsync, ::isGenerator, ::range)
}
