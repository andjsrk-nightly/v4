package io.github.andjsrk.v4.parse.node

sealed interface BindingPatternNode: BindingElementNode, NonAtomicNode {
    val elements: List<MaybeRestNode>
}
