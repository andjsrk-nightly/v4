package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.SimpleLazyFlow
import io.github.andjsrk.v4.evaluate.type.NonEmptyWideOrAbrupt

class ClassExpressionNode(
    override val name: IdentifierNode?,
    override val parent: ExpressionNode?,
    override val elements: List<ClassElementNode>,
    override val range: Range,
): ClassNode(), ExpressionNode {
    override fun evaluate(): SimpleLazyFlow<NonEmptyWideOrAbrupt> {
        TODO()
    }
}
