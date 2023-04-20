package io.github.andjsrk.v4.parse.node

interface StatementNode: Node {
    interface Unsealed: Node.Unsealed {
        override fun toSealed(): StatementNode
    }
}
