package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.stringifyLikeDataClass

class NumericLiteralNode(override val raw: String): DynamicPrimitiveLiteralNode<Double>() {
    override fun DynamicPrimitiveLiteralRaw.toActualValue() =
        this.toDouble()
    override fun toString() =
        stringifyLikeDataClass(::raw, ::value)
    class Unsealed: DynamicPrimitiveLiteralNode.Unsealed<Double>() {
        override fun toSealed() =
            NumericLiteralNode(raw)
    }
}
