package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

class ClassExpressionNode(
    override val name: IdentifierNode?,
    override val parent: ExpressionNode?,
    override val elements: List<ClassElementNode>,
    override val range: Range,
): ClassNode(), ExpressionNode
