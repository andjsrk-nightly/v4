package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range

class FormalParametersNode(
    val elements: List<MaybeRestNode>,
    override val range: Range,
): Node {
    val boundNames by lazy {
        elements.flatMap { it.boundNames }
    }
    override fun toString() = throw NotImplementedError()
}

private val IdentifierOrBindingPatternNode.boundNames get() =
    when (this) {
        is IdentifierNode -> listOf(this)
        is BindingPatternNode -> elements.flatMap { it.boundNames }
        else -> throw IllegalArgumentException()
    }
private val MaybeRestNode.boundNames: List<IdentifierNode> get() =
    `as`.boundNames
