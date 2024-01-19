package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.parse.node.ExpressionNode

internal fun ExpressionNode.evaluateValue() = lazyFlow {
    val res = yieldAll(evaluate())
        .orReturn { return@lazyFlow it }
    getValue(res)
}
