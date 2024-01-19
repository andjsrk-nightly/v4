package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class TaggedTemplateNode(
    val callee: ExpressionNode,
    val template: TemplateLiteralNode,
): ExpressionNode, NonAtomicNode {
    override val childNodes get() = listOf(callee, template)
    override val range = callee.range..template.range
    override fun toString() =
        stringifyLikeDataClass(::callee, ::template, ::range)
    override fun evaluate() = lazyFlow f@ {
        val funcRef = yieldAll(callee.evaluate())
            .orReturn { return@f it }
        val func = getValue(funcRef)
            .orReturn { return@f it }
        val args = evaluateTaggedArguments(template)
            .orReturn { return@f it }
        evaluateCall(func, funcRef, args)
    }
}
