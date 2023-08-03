package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.CompilerFalsePositive
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.NormalOrAbrupt
import io.github.andjsrk.v4.neverHappens
import io.github.andjsrk.v4.parse.node.*

internal fun evaluateConciseBody(body: ConciseBodyNode): NormalOrAbrupt {
    return when (body) {
        is ExpressionNode -> {
            val value = body.evaluateValueOrReturn { return it }
            Completion.Return(value)
        }
        is BlockNode -> body.evaluate()
        else ->
            @CompilerFalsePositive
            neverHappens()
    }
}
