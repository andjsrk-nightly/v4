package io.github.andjsrk.v4.parse.node

sealed interface MethodLikeNode: NonAtomicNode {
    val name: ObjectLiteralKeyNode
    val body: BlockStatementNode
}
