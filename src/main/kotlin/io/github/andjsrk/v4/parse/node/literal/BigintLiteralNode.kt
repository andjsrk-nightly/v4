package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.stringifyLikeDataClass
import java.math.BigInteger

class BigintLiteralNode(override val raw: String): DynamicPrimitiveLiteralNode<BigInteger>() {
    override fun DynamicPrimitiveLiteralRaw.toActualValue() =
        this.toBigInteger()
    override fun toString() =
        stringifyLikeDataClass(::raw, ::value)
    class Unsealed: DynamicPrimitiveLiteralNode.Unsealed<BigInteger>() {
        override fun toSealed() =
            BigintLiteralNode(raw)
    }
}
