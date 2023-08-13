package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.parse.node.ExpressionNode

internal fun ExpressionNode.evaluateValue(): NonEmptyNormalOrAbrupt {
    val res = evaluate().orReturn { return it }
    return getValue(res)
}
