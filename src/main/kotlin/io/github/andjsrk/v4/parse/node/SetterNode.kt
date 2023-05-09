package io.github.andjsrk.v4.parse.node

sealed interface SetterNode: MethodLikeNode {
    val parameter: NonRestNode
}
