package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

sealed class CallNode(
    val callee: ExpressionNode,
    arguments: List<MaybeSpreadNode>,
    range: Range,
): FixedCalleeCallNode(arguments, range) {
    override fun toString() =
        stringifyLikeDataClass(::callee, ::arguments, ::range)
    abstract class Unsealed: FixedCalleeCallNode.Unsealed() {
        lateinit var callee: ExpressionNode
    }
}
