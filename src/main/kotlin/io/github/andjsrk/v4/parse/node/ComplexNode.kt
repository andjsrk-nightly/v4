package io.github.andjsrk.v4.parse.node

interface ComplexNode: Node {
    interface Unsealed {
        fun toSealed(): ComplexNode
    }
}
