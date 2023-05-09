package io.github.andjsrk.v4.parse.node

class CoverInitializedNameNode(
    val key: IdentifierNode,
    val default: ExpressionNode,
): ObjectElementNode {
    override val range = key.range..default.range
    override fun toString() = throw NotImplementedError()
}
