package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.resolveBinding
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class IdentifierNode(
    val value: String,
    override val range: Range,
): ExpressionNode, ObjectLiteralKeyNode, BindingElementNode {
    override fun toString() =
        stringifyLikeDataClass(::value, ::range)
    override fun evaluate() =
        Completion.WideNormal(resolveBinding(value.languageValue))
}
