package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.ClassType
import io.github.andjsrk.v4.evaluate.type.lang.NullType

class NewExpressionNode(
    callee: ExpressionNode,
    arguments: ArgumentsNode,
    startRange: Range,
): CallNode(callee, arguments) {
    override val range = startRange..arguments.range
    override fun evaluate(): NonEmptyNormalOrAbrupt {
        val calleeRes = callee.evaluateOrReturn { return it }
        val clazz = getValueOrReturn(calleeRes) { return it }
        val args = returnIfAbrupt(evaluateArguments(arguments)) { return it }
        if (clazz !is ClassType) return Completion.Throw(NullType/* TypeError */)
        return clazz.construct(args)
    }
}
