package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ClassExpressionNode(
    val name: IdentifierNode?,
    override val parent: ExpressionNode?,
    override val elements: List<ClassElementNode>,
    override val range: Range,
): ClassNode, ExpressionNode {
    override val childNodes = listOf(name, parent) + elements
    override fun toString() =
        stringifyLikeDataClass(::name, ::parent, ::elements, ::range)
}
