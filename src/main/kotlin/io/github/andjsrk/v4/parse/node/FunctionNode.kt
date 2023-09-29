package io.github.andjsrk.v4.parse.node

sealed interface FunctionNode: NonAtomicNode {
    val parameters: UniqueFormalParametersNode
    val body: ConciseBodyNode
}
