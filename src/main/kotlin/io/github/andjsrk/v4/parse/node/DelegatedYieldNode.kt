package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

class DelegatedYieldNode(
    override val expression: ExpressionNode,
    range: Range,
): YieldNode(expression, range)
