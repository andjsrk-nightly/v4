package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class IdentifierNode(
    val value: String,
    override val range: Range,
): ExpressionNode, ObjectLiteralKeyNode, BindingElementNode {
    override fun toString() =
        stringifyLikeDataClass(::value, ::range)
    override fun evaluate() = lazyFlowNoYields {
        resolveBinding(value.languageValue)
    }
}
