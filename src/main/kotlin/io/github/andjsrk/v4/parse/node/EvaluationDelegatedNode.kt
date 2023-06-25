package io.github.andjsrk.v4.parse.node

sealed interface EvaluationDelegatedNode: Node {
    override fun evaluate() =
        throw NotImplementedError("The node does not implement method `evaluate` intentionally because it will be used only as a container.")
}
