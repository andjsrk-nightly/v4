package io.github.andjsrk.v4.parse.node

sealed interface MaybeSpreadNode: NonAtomicNode, ObjectElementNode, EvaluationDelegatedNode {
    val expression: ExpressionNode
}
