package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

class DefaultExportDeclarationNode(
    val expression: ExpressionNode,
    startRange: Range,
    semicolonRange: Range?,
): ExportDeclarationNode {
    override val range = startRange..(semicolonRange ?: expression.range)
}
