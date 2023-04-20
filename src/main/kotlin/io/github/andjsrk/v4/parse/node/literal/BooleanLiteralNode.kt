package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.stringifyLikeDataClass

class BooleanLiteralNode(override val raw: DynamicPrimitiveLiteralRaw): DynamicPrimitiveLiteralNode<Boolean>() {
    override fun DynamicPrimitiveLiteralRaw.toActualValue() =
        this.toBoolean()
    override fun toString() =
        stringifyLikeDataClass(::raw, ::value)
    class Unsealed: DynamicPrimitiveLiteralNode.Unsealed<Boolean>() {
        override fun toSealed() =
            BooleanLiteralNode(raw)
    }
}
