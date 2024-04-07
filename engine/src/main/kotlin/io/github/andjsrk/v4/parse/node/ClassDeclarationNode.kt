package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.toNormal

class ClassDeclarationNode(
    override val name: IdentifierNode,
    override val parent: ExpressionNode?,
    override val elements: List<ClassElementNode>,
    override val range: Range,
): ClassNode(), DeclarationNode {
    override fun evaluate() = lazyFlow f@ {
        val name = name.value
        val value = yieldAll(evaluateTail())
            .orReturn { return@f it }
        runningExecutionContext.lexicalEnvNotNull.initializeBinding(name, value).unwrap()
        value.toNormal()
    }
}
