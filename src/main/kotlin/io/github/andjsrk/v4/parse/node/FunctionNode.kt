package io.github.andjsrk.v4.parse.node

sealed interface FunctionNode: NonAtomicNode {
    val body: Node // body can be an expression in arrow functions
}
