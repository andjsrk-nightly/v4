package io.github.andjsrk.v4.parse.node

interface ExpressionNode: Node {
    interface Unsealed: Node.Unsealed {
        override fun toSealed(): ExpressionNode
    }
}
