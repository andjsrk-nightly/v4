package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NamespaceImportBindingNode(
    val binding: IdentifierNode,
    startRange: Range,
): NonDefaultImportBindingNode {
    override val childNodes get() = listOf(binding)
    override val range = startRange..binding.range
    override fun toString() =
        stringifyLikeDataClass(::binding, ::range)
}
