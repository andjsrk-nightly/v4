package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.parse.node.ExpressionNode
import io.github.andjsrk.v4.stringifyLikeDataClass

private typealias Actual = Array<ExpressionNode>

class ArrayLiteralNode(override val value: Actual): DynamicLiteralNode<Actual> {
    override fun toString() =
        stringifyLikeDataClass(::value)
    class Unsealed: DynamicLiteralNode.Unsealed<Actual> {
        val value = mutableListOf<ExpressionNode>()
        override fun toSealed() =
            ArrayLiteralNode(value.toTypedArray())
    }
}
