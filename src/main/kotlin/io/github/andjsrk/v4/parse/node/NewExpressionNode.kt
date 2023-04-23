package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

class NewExpressionNode(
    callee: ExpressionNode,
    arguments: List<CommaSeparatedElementNode>,
    range: Range,
): CallExpressionNode(callee, arguments, range) {
    class Unsealed: CallExpressionNode.Unsealed() {
        lateinit var startRange: Range
        override fun toSealed() =
            NewExpressionNode(callee, arguments.toList(), startRange until endRange)
    }
}
