package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token
import java.math.BigInteger

class BigintLiteralNode(token: Token): DynamicPrimitiveLiteralNode<BigInteger>() {
    override val raw = token.literal
    override val range = token.range
    override fun DynamicPrimitiveLiteralRaw.toActualValue() =
        this.toBigInteger()
    override fun toString() =
        stringifyLikeDataClass(::raw, ::value, ::range)
}
