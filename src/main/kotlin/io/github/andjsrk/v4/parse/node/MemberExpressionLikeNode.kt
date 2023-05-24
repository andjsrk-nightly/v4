package io.github.andjsrk.v4.parse.node

sealed interface MemberExpressionLikeNode: ExpressionNode, NonAtomicNode {
    val `object`: ExpressionNode
    val property: ExpressionNode
    val isComputed: Boolean
}
