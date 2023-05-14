package io.github.andjsrk.v4.parse.node

sealed interface NormalFunctionNode: FunctionNode {
    val isAsync: Boolean
    val isGenerator: Boolean
}
