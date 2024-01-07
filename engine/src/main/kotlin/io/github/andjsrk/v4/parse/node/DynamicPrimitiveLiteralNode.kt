package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

sealed class DynamicPrimitiveLiteralNode<Actual>(rawContent: String, range: Range): PrimitiveLiteralNode(rawContent, range) {
    abstract val value: Actual
    override fun toString() =
        stringifyLikeDataClass(::raw, ::value, ::range)
}
