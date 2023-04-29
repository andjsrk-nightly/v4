package io.github.andjsrk.v4.parse.node

sealed interface CollectionLiteralNode: LiteralNode {
    val elements: List<MaybeSpreadNode>
}
