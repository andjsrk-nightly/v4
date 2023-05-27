package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NamedExportDeclarationNode(
    val elements: List<ImportOrExportSpecifierNode>,
    val moduleSpecifier: StringLiteralNode?,
    startRange: Range,
    endRange: Range,
    semicolonRange: Range?,
): ExportDeclarationNode {
    override val childNodes get() = elements + moduleSpecifier
    override val range = startRange..(semicolonRange ?: moduleSpecifier?.range ?: endRange)
    override fun toString() =
        stringifyLikeDataClass(::elements, ::moduleSpecifier, ::range)
}
