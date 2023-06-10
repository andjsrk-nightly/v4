package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.parse.node.ExpressionNode
import io.github.andjsrk.v4.parse.node.ParenthesizedExpressionNode

internal fun ExpressionNode.evaluateWithName(name: LanguageType): Completion {
    if (this is ParenthesizedExpressionNode) return expression.evaluateWithName(name)
    TODO()
}

internal inline fun ExpressionNode.evaluateWithNameOrReturn(name: LanguageType, `return`: CompletionReturn): LanguageType =
    getLanguageTypeOrReturn(evaluateWithName(name), `return`)
