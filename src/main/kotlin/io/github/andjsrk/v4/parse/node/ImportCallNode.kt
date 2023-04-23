package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ImportCallNode(
    val pathSpecifier: ExpressionNode,
    range: Range,
): CallSyntaxExpressionNode(range) {
    override fun toString() =
        stringifyLikeDataClass(::pathSpecifier, ::range)
    class Unsealed: CallSyntaxExpressionNode.Unsealed() {
        lateinit var pathSpecifier: ExpressionNode
        override fun toSealed() =
            ImportCallNode(pathSpecifier, startRange until endRange)
    }
}
