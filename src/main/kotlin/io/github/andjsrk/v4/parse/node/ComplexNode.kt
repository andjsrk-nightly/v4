package io.github.andjsrk.v4.parse.node

sealed interface ComplexNode: Node {
    sealed interface Unsealed {
        fun toSealed(): ComplexNode
    }
}
