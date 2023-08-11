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
    override fun evaluate() =
        EvalFlow {
            val calleeRes = callee.evaluate()
                .returnIfAbrupt(this) { return@EvalFlow }
            val calleeValue = getValue(calleeRes)
                .returnIfAbrupt { `return`(it) }
            val args = evaluateArguments(arguments)
                .returnIfAbrupt(this) { return@EvalFlow }
            val clazz = calleeValue.requireToBe<ClassType> { `return`(it) }
            `return`(clazz.construct(args))
        }
}
