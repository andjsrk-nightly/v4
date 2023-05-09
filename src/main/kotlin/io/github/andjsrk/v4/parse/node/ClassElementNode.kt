package io.github.andjsrk.v4.parse.node

sealed interface ClassElementNode: Node {
    val isStatic: Boolean
}
