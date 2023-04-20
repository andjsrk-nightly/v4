package io.github.andjsrk.v4.parse.node

interface Node {
    interface Unsealed {
        fun toSealed(): Node
    }
}
