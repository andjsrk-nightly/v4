package io.github.andjsrk.v4.parse.node

sealed interface HasParameters: NonAtomicNode {
    val parameters: UniqueFormalParametersNode
}
