package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

class SuperCallNode(
    superNode: SuperNode,
    arguments: Arguments,
    range: Range,
): CallExpressionNode(superNode, arguments, range) {
    class Unsealed: FixedCalleeCallExpressionNode.Unsealed() {
        lateinit var superNode: SuperNode
        override fun toSealed() =
            SuperCallNode(superNode, arguments.toList(), superNode.range until endRange)
    }
}
