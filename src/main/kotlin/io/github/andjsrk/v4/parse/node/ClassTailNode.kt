package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

internal class ClassTailNode(
    val elements: List<ClassElementNode>,
    override val range: Range,
): Node {
    override fun toString() = throw NotImplementedError()
}
