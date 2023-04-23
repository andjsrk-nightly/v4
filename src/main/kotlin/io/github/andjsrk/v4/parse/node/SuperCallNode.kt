package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

class SuperCallNode(
    arguments: List<CommaSeparatedElementNode>,
    range: Range,
): FixedCalleeCallExpressionNode(arguments, range) {
    class Unsealed: FixedCalleeCallExpressionNode.Unsealed() {
        override fun toSealed() =
            SuperCallNode(arguments.toList(), startRange until endRange)
    }
}
