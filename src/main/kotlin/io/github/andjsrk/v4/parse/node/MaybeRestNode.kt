package io.github.andjsrk.v4.parse.node

sealed interface MaybeRestNode: NonAtomicNode {
    val binding: BindingElementNode
}
