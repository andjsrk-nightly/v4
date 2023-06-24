package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.NonEmpty
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class TaggedTemplateNode(
    val callee: ExpressionNode,
    val template: TemplateLiteralNode,
): ExpressionNode, NonAtomicNode {
    override val childNodes get() = listOf(callee, template)
    override val range = callee.range..template.range
    override fun toString() =
        stringifyLikeDataClass(::callee, ::template, ::range)
    override fun evaluate(): NonEmpty {
        val funcRef = callee.evaluateOrReturn { return it }
        val func = getValueOrReturn(funcRef) { return it }
        val args = returnIfAbrupt(evaluateTaggedArguments(template)) { return it }
        return evaluateCall(func, funcRef, args)
    }
}
