package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

class NumericLiteralNode(token: Token): DynamicPrimitiveLiteralNode<Double>() {
    override val raw = token.literal
    override val range = token.range
    override fun DynamicPrimitiveLiteralRaw.toActualValue() =
        this.toDouble()
    override fun toString() =
        stringifyLikeDataClass(::raw, ::value, ::range)
}
