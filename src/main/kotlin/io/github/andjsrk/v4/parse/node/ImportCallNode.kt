package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ImportCallNode(
    importNode: ImportNode,
    val pathSpecifier: ExpressionNode,
    endRange: Range,
): FixedArgumentCallNode(importNode) {
    override val range = importNode.range..endRange
    override val childNodes = super.childNodes + pathSpecifier
    override fun toString() =
        stringifyLikeDataClass(::callee, ::pathSpecifier, ::range)
}
