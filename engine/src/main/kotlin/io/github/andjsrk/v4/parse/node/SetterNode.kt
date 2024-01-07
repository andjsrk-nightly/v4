package io.github.andjsrk.v4.parse.node

sealed interface SetterNode: MethodNode {
    val parameter: NonRestNode
}
