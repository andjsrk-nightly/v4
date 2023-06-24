package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.parse.node.*

internal fun ConciseBodyNode.evaluateAsConciseBody() =
    when (this) {
        is ExpressionNode -> evaluateAsExpressionBody()
        is BlockNode -> evaluate()
    }
