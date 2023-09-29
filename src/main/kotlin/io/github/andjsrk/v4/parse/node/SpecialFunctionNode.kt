package io.github.andjsrk.v4.parse.node

sealed interface SpecialFunctionNode: FunctionNode {
    val isAsync: Boolean
    val isGenerator: Boolean
}
