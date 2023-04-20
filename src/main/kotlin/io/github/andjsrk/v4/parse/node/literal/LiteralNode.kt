package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.parse.node.ExpressionNode

interface LiteralNode: ExpressionNode {
    interface Unsealed: ExpressionNode.Unsealed {
        override fun toSealed(): LiteralNode
    }
}
