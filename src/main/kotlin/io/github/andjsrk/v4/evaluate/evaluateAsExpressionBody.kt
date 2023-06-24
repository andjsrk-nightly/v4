package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.parse.node.*

internal fun ExpressionNode.evaluateAsExpressionBody(): NonEmptyNormalOrAbrupt {
    val value = evaluateValueOrReturn { return it }
    return Completion.Return(value)
}
