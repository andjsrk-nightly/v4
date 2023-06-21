package io.github.andjsrk.v4.parse.node

sealed interface FunctionWithoutParameterNode: NonAtomicNode {
    // getter/setters have static parameter count, so no `parameters` here
    val body: ConciseBodyNode
}
