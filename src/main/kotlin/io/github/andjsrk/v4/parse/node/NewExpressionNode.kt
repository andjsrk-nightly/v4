package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

class NewExpressionNode(
    callee: ExpressionNode,
    arguments: List<MaybeSpreadNode>,
    range: Range,
): CallNode(callee, arguments, range) {
    class Unsealed: CallNode.Unsealed() {
        lateinit var startRange: Range
        override fun toSealed() =
            NewExpressionNode(callee, arguments.toList(), startRange..endRange)
    }
}
