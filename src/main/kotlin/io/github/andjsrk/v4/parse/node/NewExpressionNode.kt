package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.ClassType

class NewExpressionNode(
    callee: ExpressionNode,
    arguments: ArgumentsNode,
    startRange: Range,
): CallNode(callee, arguments) {
    override val range = startRange..arguments.range
    override fun evaluate(): NonEmptyNormalOrAbrupt {
        val calleeValue = callee.evaluateValue()
            .orReturn { return it }
        val args = arguments.evaluate()
            .orReturn { return it }
        val clazz = calleeValue.requireToBe<ClassType> { return it }
        return clazz.construct(args)
    }
}
