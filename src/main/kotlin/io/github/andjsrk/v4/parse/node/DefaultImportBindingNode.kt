package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class DefaultImportBindingNode(
    val binding: IdentifierNode,
): ImportBindingNode {
    override val childNodes get() = listOf(binding)
    override val range = binding.range
    override fun toString() =
        stringifyLikeDataClass(::binding, ::range)
}
