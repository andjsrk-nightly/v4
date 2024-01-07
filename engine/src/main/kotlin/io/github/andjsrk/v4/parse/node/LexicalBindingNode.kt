package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class LexicalBindingNode(
    val element: BindingElementNode,
    val value: ExpressionNode?,
): NonAtomicNode, EvaluationDelegatedNode {
    override val childNodes get() = listOf(element, value)
    override val range = element.range.extendCarefully(value?.range)
    override fun toString() =
        stringifyLikeDataClass(::element, ::value, ::range)
}
