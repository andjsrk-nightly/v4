package io.github.andjsrk.v4.parse.node

sealed interface BlockNode: NonAtomicNode {
    val statements: List<StatementNode>
}
