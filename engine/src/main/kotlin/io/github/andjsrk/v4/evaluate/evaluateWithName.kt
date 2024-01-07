package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.FunctionType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey
import io.github.andjsrk.v4.missingBranch
import io.github.andjsrk.v4.parse.node.*

internal fun ExpressionNode.evaluateWithName(name: PropertyKey): FunctionType {
    if (this is ParenthesizedExpressionNode) return expression.evaluateWithName(name)
    return when (this) {
        is ArrowFunctionNode -> instantiateArrowFunction(name)
        is MethodExpressionNode -> instantiateMethod(name)
        is ClassExpressionNode -> TODO()
        else -> missingBranch()
    }
}
