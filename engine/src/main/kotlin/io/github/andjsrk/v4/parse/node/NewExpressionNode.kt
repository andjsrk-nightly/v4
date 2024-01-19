package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.ClassType

class NewExpressionNode(
    callee: ExpressionNode,
    arguments: ArgumentsNode,
    startRange: Range,
): CallNode(callee, arguments) {
    override val range = startRange..arguments.range
    override fun evaluate() = lazyFlow f@ {
        val calleeValue = yieldAll(callee.evaluateValue())
            .orReturn { return@f it }
        val args = yieldAll(arguments.evaluate())
            .orReturn { return@f it }
        val clazz = calleeValue.requireToBe<ClassType> { return@f it }
        clazz.construct(args)
    }
}
