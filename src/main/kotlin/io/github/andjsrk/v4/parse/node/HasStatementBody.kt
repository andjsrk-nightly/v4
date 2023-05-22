package io.github.andjsrk.v4.parse.node

sealed interface HasStatementBody: NonAtomicNode {
    val body: StatementNode
}
