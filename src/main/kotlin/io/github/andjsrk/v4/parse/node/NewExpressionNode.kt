package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

class NewExpressionNode(
    callee: ExpressionNode,
    arguments: ArgumentsNode,
    startRange: Range,
): CallNode(callee, arguments) {
    override val range = startRange..arguments.range
}
