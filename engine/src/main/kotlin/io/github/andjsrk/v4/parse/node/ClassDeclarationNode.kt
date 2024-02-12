package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.lazyFlow
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.parse.stringValue

class ClassDeclarationNode(
    override val name: IdentifierNode,
    override val parent: ExpressionNode?,
    override val elements: List<ClassElementNode>,
    override val range: Range,
): ClassNode(), DeclarationNode {
    override fun evaluate() = lazyFlow {
        val name = name.stringValue
        val value = evaluateTail(true)
        empty
    }
}
