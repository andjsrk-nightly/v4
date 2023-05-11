package io.github.andjsrk.v4.parse.node

sealed interface FunctionNode: NonAtomicNode {
    val parameters: FormalParametersNode
    val body: Node // body can be an expression in arrow functions
    val isAsync: Boolean
    val isGenerator: Boolean
}
