package io.github.andjsrk.v4.parse.node

sealed interface SpecialFunctionNode: NonSpecialFunctionNode {
    val isAsync: Boolean
    val isGenerator: Boolean
}
