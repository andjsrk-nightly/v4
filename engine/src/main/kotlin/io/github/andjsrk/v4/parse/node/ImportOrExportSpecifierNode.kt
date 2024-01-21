package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ImportOrExportSpecifierNode(
    val name: IdentifierNode,
    val alias: IdentifierNode,
): NonAtomicNode, EvaluationDelegatedNode {
    override val childNodes get() = listOf(name, alias)
    override val range = name.range..alias.range
    override fun toString() =
        stringifyLikeDataClass(::name, ::alias, ::range)
}
