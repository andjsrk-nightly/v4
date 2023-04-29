package io.github.andjsrk.v4.parse.node

sealed interface MaybeRestNode: Node {
    val `as`: Node
}
