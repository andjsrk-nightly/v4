package io.github.andjsrk.v4.parse.node

sealed interface StatementListNode: NonAtomicNode {
    val elements: List<StatementNode>
}
