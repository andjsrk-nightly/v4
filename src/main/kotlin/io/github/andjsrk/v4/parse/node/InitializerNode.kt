package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

internal class InitializerNode(
    val value: ExpressionNode,
    startRange: Range,
): NonAtomicNode {
    override val childNodes get() = listOf(value)
    override val range = startRange..value.range
    override fun toString() = throw NotImplementedError()
}
