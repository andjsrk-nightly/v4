package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.UnaryOperationType

class UpdateExpressionNode(
    operand: ExpressionNode,
    operation: UnaryOperationType,
    operationTokenRange: Range,
    isPrefixed: Boolean = true,
): UnaryExpressionNode(operand, operation, operationTokenRange, isPrefixed)
