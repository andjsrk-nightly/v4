package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.parse.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

class NumberLiteralNode(token: Token): DynamicPrimitiveLiteralNode<Double>(token) {
    override val value by lazy {
        raw.toDouble()
    }
    override fun toString() =
        stringifyLikeDataClass(::raw, ::value, ::range)
}
