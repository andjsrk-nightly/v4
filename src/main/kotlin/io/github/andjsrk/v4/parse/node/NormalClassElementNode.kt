package io.github.andjsrk.v4.parse.node

sealed interface NormalClassElementNode: ClassElementNode, NonAtomicNode {
    val isStatic: Boolean
}
