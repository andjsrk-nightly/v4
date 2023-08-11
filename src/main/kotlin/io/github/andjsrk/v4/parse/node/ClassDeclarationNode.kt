package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

class ClassDeclarationNode(
    override val name: IdentifierNode,
    override val parent: ExpressionNode?,
    override val elements: List<ClassElementNode>,
    override val range: Range,
): ClassNode(), DeclarationNode {
    override fun evaluate() = TODO()
}
