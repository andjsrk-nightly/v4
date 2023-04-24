package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.parse.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

abstract class DynamicPrimitiveLiteralNode<Actual>(token: Token): PrimitiveLiteralNode(token) {
    abstract val value: Actual
    override fun toString() =
        stringifyLikeDataClass(::raw, ::value, ::range)
}