package io.github.andjsrk.v4.parse.node

sealed interface MethodLikeNode: Node {
    val name: ObjectLiteralKeyNode
    val body: BlockStatementNode
}
