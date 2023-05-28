package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.flatZip
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class TemplateLiteralNode(
    val strings: List<TemplateStringNode>,
    val expressions: List<ExpressionNode>,
): ExpressionNode, NonAtomicNode {
    override val childNodes get() = strings.flatZip(expressions)
    override val range = strings.first().range..strings.last().range
    override fun toString() =
        stringifyLikeDataClass(::strings, ::expressions, ::range)
}
