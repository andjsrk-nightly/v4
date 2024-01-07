package io.github.andjsrk.v4.parse.node

/**
 * Indicates a [Node] that intentionally does not implement method [evaluate] because it will be used only as a container.
 */
sealed interface EvaluationDelegatedNode: Node {
    override fun evaluate() =
        throw NotImplementedError("The node intentionally does not implement method `evaluate` because it will be used only as a container.")
}
