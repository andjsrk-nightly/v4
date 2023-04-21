package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

interface Node {
    val range: Range
    interface Unsealed {
        fun toSealed(): Node
    }
}
