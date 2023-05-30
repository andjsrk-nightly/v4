package io.github.andjsrk.v4.parse.node

sealed interface SpecialFunctionNode: FunctionWithoutParameterNode, HasParameters {
    val isAsync: Boolean
    val isGenerator: Boolean
}
