package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NamedSingleExportDeclarationNode(
    val declaration: DeclarationNode,
    startRange: Range,
    semicolonRange: Range?,
): ExportDeclarationNode {
    override val childNodes get() = listOf(declaration)
    override val range = startRange..(semicolonRange ?: declaration.range)
    override fun toString() =
        stringifyLikeDataClass()
}
