package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormal
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey
import io.github.andjsrk.v4.missingBranch
import io.github.andjsrk.v4.parse.node.*

internal fun ExpressionNode.evaluateWithName(name: PropertyKey): NonEmptyNormal {
    if (this is ParenthesizedExpressionNode) return expression.evaluateWithName(name)
    return Completion.Normal(
        when (this) {
            is ArrowFunctionNode -> instantiateArrowFunction(name)
            is MethodExpressionNode -> TODO()
            is ClassExpressionNode -> TODO()
            else -> missingBranch()
        }
    )
}

internal inline fun ExpressionNode.evaluateWithNameOrReturn(name: PropertyKey, `return`: AbruptReturnLambda): LanguageType =
    evaluateWithName(name).returnIfAbrupt(`return`)
