package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

internal class ClassTailNode(
    val parent: ExpressionNode?,
    val elements: List<ClassElementNode>,
    override val range: Range,
): NonAtomicNode, EvaluationDelegatedNode {
    override val childNodes get() = listOf(parent) + elements
    override fun toString() = throw NotImplementedError()
}
