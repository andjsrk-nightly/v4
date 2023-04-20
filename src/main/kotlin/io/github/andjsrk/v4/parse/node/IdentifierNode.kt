package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.stringifyLikeDataClass

class IdentifierNode(val value: String): ExpressionNode {
    override fun toString() =
        stringifyLikeDataClass(::value)
    class Unsealed: ExpressionNode.Unsealed {
        var value = ""
        override fun toSealed() =
            IdentifierNode(value)
    }
}
