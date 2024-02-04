package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.CompilerFalsePositive
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens
import io.github.andjsrk.v4.parse.node.*

internal fun evaluateConciseBody(body: ConciseBodyNode) = lazyFlow f@ {
    when (body) {
        is ExpressionNode -> {
            val value = yieldAll(body.evaluateValue().asFromFunctionBody())
                .orReturnNonEmpty { return@f it }
            Completion.Return(value)
        }
        is BlockNode -> yieldAll(
            body.evaluate()
                .asFromFunctionBody()
                .asNotNullable()
        )
        else ->
            @CompilerFalsePositive
            neverHappens()
    }
}
