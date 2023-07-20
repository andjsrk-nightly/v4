package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.CompilerFalsePositive
import io.github.andjsrk.v4.neverHappens
import io.github.andjsrk.v4.parse.node.*

internal fun evaluateConciseBody(body: ConciseBodyNode) =
    when (body) {
        is ExpressionNode -> body.evaluateAsExpressionBody()
        is BlockNode -> body.evaluate()
        else ->
            @CompilerFalsePositive
            neverHappens()
    }
