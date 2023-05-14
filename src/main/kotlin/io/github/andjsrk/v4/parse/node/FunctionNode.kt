package io.github.andjsrk.v4.parse.node

/**
 * Represents a function without keywords such as `async`.
 */
sealed interface FunctionNode: NonAtomicNode {
    val parameters: FormalParametersNode
    val body: Node // body can be an expression in arrow functions
}
