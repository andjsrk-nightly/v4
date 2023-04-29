package io.github.andjsrk.v4.parse.node

sealed interface BindingPatternNode: Node {
    val elements: List<MaybeRestNode>
}
