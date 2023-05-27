package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NamespaceExportDeclarationNode(
    val binding: IdentifierNode,
    val moduleSpecifier: StringLiteralNode,
    startRange: Range,
    semicolonRange: Range?,
): ExportDeclarationNode {
    override val childNodes get() = listOf(binding, moduleSpecifier)
    override val range = startRange..(semicolonRange ?: moduleSpecifier.range)
    override fun toString() =
        stringifyLikeDataClass(::binding, ::moduleSpecifier, ::range)
}
