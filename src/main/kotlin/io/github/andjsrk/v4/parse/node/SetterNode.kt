package io.github.andjsrk.v4.parse.node

sealed interface SetterNode: FixedParametersMethodNode {
    val parameter: NonRestNode
}
