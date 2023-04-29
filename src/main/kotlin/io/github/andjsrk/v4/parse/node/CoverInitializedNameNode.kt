package io.github.andjsrk.v4.parse.node

class CoverInitializedNameNode(
    val key: IdentifierNode,
    val default: ExpressionNode,
): ExpressionNode/* for compatibility */ {
    override val range = key.range..default.range
    override fun toString() = throw NotImplementedError()
}
