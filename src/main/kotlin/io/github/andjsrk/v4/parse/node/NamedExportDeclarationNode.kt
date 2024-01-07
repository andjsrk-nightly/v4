package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NamedExportDeclarationNode(
    val specifiers: List<ImportOrExportSpecifierNode>,
    range: Range,
    semicolonRange: Range?,
): ExportDeclarationNode {
    override val childNodes = specifiers
    override val range = range.extendCarefully(semicolonRange)
    override fun toString() =
        stringifyLikeDataClass(::specifiers, ::range)
}
