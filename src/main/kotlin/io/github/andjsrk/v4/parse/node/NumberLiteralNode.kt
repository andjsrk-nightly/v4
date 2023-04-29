package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

class NumberLiteralNode(token: Token): DynamicPrimitiveLiteralNode<Double>(token), ObjectLiteralKeyNode {
    override val value by lazy {
        raw.toDouble()
    }
    override fun toString() =
        stringifyLikeDataClass(::raw, ::value, ::range)
}
