package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ImportCallNode(
    val importNode: ImportNode,
    val pathSpecifier: ExpressionNode,
    range: Range,
): CallSyntaxExpressionNode(range) {
    override fun toString() =
        stringifyLikeDataClass(::importNode, ::pathSpecifier, ::range)
    class Unsealed: CallSyntaxExpressionNode.Unsealed() {
        lateinit var importNode: ImportNode
        lateinit var pathSpecifier: ExpressionNode
        override fun toSealed() =
            ImportCallNode(importNode, pathSpecifier, importNode.range until endRange)
    }
}
