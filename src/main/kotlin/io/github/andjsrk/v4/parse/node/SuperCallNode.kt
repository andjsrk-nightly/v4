package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

class SuperCallNode(
    superNode: SuperNode,
    arguments: List<MaybeSpreadNode>,
    range: Range,
): CallNode(superNode, arguments, range) {
    class Unsealed: FixedCalleeCallNode.Unsealed() {
        lateinit var superNode: SuperNode
        override fun toSealed() =
            SuperCallNode(superNode, arguments.toList(), superNode.range..endRange)
    }
}
